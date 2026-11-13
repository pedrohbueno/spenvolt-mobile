package com.spenvolt.mobile.ui.residence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.ui.device.DeviceAdapter;
import com.spenvolt.mobile.ui.device.DeviceDetailActivity;
import com.spenvolt.mobile.ui.device.DeviceFormActivity;
import com.spenvolt.mobile.ui.member.MemberFormActivity;
import com.spenvolt.mobile.util.Format;

import java.util.List;

public class ResidenceActivity extends AppCompatActivity {

    private SessionManager session;

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
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_residence, 0)) return;

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

        findViewById(R.id.panelDevices).setVisibility(tab == Tab.DEVICES ? View.VISIBLE : View.GONE);
        findViewById(R.id.panelMembers).setVisibility(tab == Tab.MEMBERS ? View.VISIBLE : View.GONE);
        findViewById(R.id.panelPlan).setVisibility(tab == Tab.PLAN ? View.VISIBLE : View.GONE);

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
                findViewById(R.id.txtError).setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                ((TextView) findViewById(R.id.txtError)).setText("Residência não encontrada ou API indisponível.");
                findViewById(R.id.txtError).setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadDevices() {
        new DeviceApi(this).findByResidence(residenceId, new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> devices) {
                deviceAdapter.setDevices(devices);
                floorPlan.setDevices(devices);
                View empty = findViewById(R.id.emptyDevices);
                empty.setVisibility(devices.isEmpty() ? View.VISIBLE : View.GONE);
                ((TextView) empty.findViewById(R.id.txtEmptyTitle)).setText("Nenhum dispositivo cadastrado ainda");
                ((TextView) empty.findViewById(R.id.txtEmptyText)).setText("Adicione os aparelhos da casa no botão + para acompanhar o consumo de cada um.");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResidenceActivity.this, message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ResidenceActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMembers(List<Member> members) {
        membersContainer.removeAllViews();
        View empty = findViewById(R.id.emptyMembers);
        empty.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView) empty.findViewById(R.id.txtEmptyTitle)).setText("Nenhum membro cadastrado ainda");
        ((TextView) empty.findViewById(R.id.txtEmptyText)).setText("Adicione quem mora na casa para ver quanto cada pessoa consome.");

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
