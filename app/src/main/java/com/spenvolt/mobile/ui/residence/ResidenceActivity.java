package com.spenvolt.mobile.ui.residence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.ui.device.DeviceAdapter;
import com.spenvolt.mobile.ui.device.DeviceDetailActivity;
import com.spenvolt.mobile.ui.device.DeviceFormActivity;
import com.spenvolt.mobile.ui.member.MemberFormActivity;
import com.spenvolt.mobile.util.Format;

import java.util.List;

public class ResidenceActivity extends BaseActivity {

    public static final String EXTRA_RESIDENCE_ID = "residence_id";

    private enum Tab { DEVICES, MEMBERS, PLAN }

    private int residenceId;
    private Tab tab = Tab.DEVICES;

    private DeviceAdapter deviceAdapter;
    private FloorPlanSection floorPlan;
    private LinearLayout membersContainer;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_residence, NAV_NONE);

        residenceId = getIntent().getIntExtra(EXTRA_RESIDENCE_ID, -1);
        session.setSelectedResidenceId(residenceId);

        deviceAdapter = new DeviceAdapter(this::openDevice);
        RecyclerView recyclerDevices = findViewById(R.id.recyclerDevices);
        recyclerDevices.setLayoutManager(new LinearLayoutManager(this));
        recyclerDevices.setAdapter(deviceAdapter);

        membersContainer = findViewById(R.id.membersContainer);
        floorPlan = new FloorPlanSection(this, findViewById(R.id.panelPlan), residenceId);
        floorPlan.load();

        findViewById(R.id.tabDevices).setOnClickListener(v -> selectTab(Tab.DEVICES));
        findViewById(R.id.tabMembers).setOnClickListener(v -> selectTab(Tab.MEMBERS));
        findViewById(R.id.tabPlan).setOnClickListener(v -> selectTab(Tab.PLAN));

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResidenceSettingsActivity.class);
            intent.putExtra(ResidenceSettingsActivity.EXTRA_RESIDENCE_ID, residenceId);
            startActivity(intent);
        });

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> onAddClicked());

        selectTab(Tab.DEVICES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResidence();
        loadDevices();
        loadMembers();
    }

    private void selectTab(Tab newTab) {
        tab = newTab;
        findViewById(R.id.tabDevices).setSelected(tab == Tab.DEVICES);
        findViewById(R.id.tabMembers).setSelected(tab == Tab.MEMBERS);
        findViewById(R.id.tabPlan).setSelected(tab == Tab.PLAN);

        Views.show(findViewById(R.id.panelDevices), tab == Tab.DEVICES);
        Views.show(findViewById(R.id.panelMembers), tab == Tab.MEMBERS);
        Views.show(findViewById(R.id.panelPlan), tab == Tab.PLAN);

        if (tab == Tab.PLAN) fabAdd.hide();
        else fabAdd.show();
    }

    private void onAddClicked() {
        Intent intent;
        if (tab == Tab.MEMBERS) {
            intent = new Intent(this, MemberFormActivity.class);
            intent.putExtra(MemberFormActivity.EXTRA_RESIDENCE_ID, residenceId);
        } else {
            intent = new Intent(this, DeviceFormActivity.class);
            intent.putExtra(DeviceFormActivity.EXTRA_RESIDENCE_ID, residenceId);
        }
        startActivity(intent);
    }

    private void loadResidence() {
        new ResidenceApi(this).findById(residenceId, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence residence) {
                ((TextView) findViewById(R.id.txtName)).setText(residence.name);
                ((TextView) findViewById(R.id.txtAddress)).setText(residence.address);
                Views.message(findViewById(R.id.txtError), null);
            }

            @Override
            public void onError(String message) {
                Views.message(findViewById(R.id.txtError), "Residência não encontrada ou API indisponível.");
            }
        });
    }

    private void loadDevices() {
        new DeviceApi(this).findByResidence(residenceId, new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> devices) {
                deviceAdapter.setDevices(devices);
                floorPlan.setDevices(devices);
                Views.empty(findViewById(R.id.emptyDevices), devices.isEmpty(),
                        "Nenhum dispositivo cadastrado ainda",
                        "Adicione os aparelhos da casa no botão + para acompanhar o consumo de cada um.");
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void loadMembers() {
        new MemberApi(this).findByResidence(residenceId, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> members) {
                renderMembers(members);
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void renderMembers(List<Member> members) {
        membersContainer.removeAllViews();
        Views.empty(findViewById(R.id.emptyMembers), members.isEmpty(),
                "Nenhum membro cadastrado ainda",
                "Adicione quem mora na casa para ver quanto cada pessoa consome.");

        for (Member m : members) {
            View row = getLayoutInflater().inflate(R.layout.item_member_row, membersContainer, false);
            ((TextView) row.findViewById(R.id.txtAvatar)).setText(Format.initial(m.name));
            ((TextView) row.findViewById(R.id.txtName)).setText(m.name);
            ((TextView) row.findViewById(R.id.txtEmail)).setText(m.email);
            membersContainer.addView(row);
        }
    }

    private void openDevice(Device device) {
        Intent intent = new Intent(this, DeviceDetailActivity.class);
        intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.id);
        startActivity(intent);
    }
}
