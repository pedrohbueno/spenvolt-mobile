package com.spenvolt.mobile.ui.member;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.model.Usage;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.ui.residence.ResidenceFormActivity;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MembersActivity extends AppCompatActivity implements MemberAdapter.Listener {

    private SessionManager session;

    private final List<Member> members = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();

    private Residence residence;
    private Member activeMember;
    private MemberAdapter adapter;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_members, R.id.navMembers)) return;
        txtStatus = findViewById(R.id.txtStatus);

        adapter = new MemberAdapter(this);
        RecyclerView recycler = findViewById(R.id.recyclerMembers);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int id = session.getSelectedResidenceId();
        if (id == -1) {
            showResidence(null);
            return;
        }
        new ResidenceApi(this).findById(id, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence result) {
                showResidence(result);
            }

            @Override
            public void onError(String message) {
                showResidence(null);
            }
        });
    }

    private void showResidence(@Nullable Residence selected) {
        residence = selected;
        if (selected == null) {
            txtStatus.setText("Cadastre uma residência para adicionar membros.");
            txtStatus.setVisibility(View.VISIBLE);
            return;
        }
        loadData();
    }

    private void loadData() {
        ((TextView) findViewById(R.id.txtResidence)).setText(residence.name);
        int residenceId = residence.id;

        new MemberApi(this).findByResidence(residenceId, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> result) {
                members.clear();
                members.addAll(result);
                txtStatus.setText("Nenhum membro cadastrado ainda. Adicione quem mora na casa para ver quanto cada pessoa consome.");
                txtStatus.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
                render();
            }

            @Override
            public void onError(String message) {
                txtStatus.setText("Não foi possível carregar os membros. Tente novamente.");
                txtStatus.setVisibility(View.VISIBLE);
            }
        });

        new DeviceApi(this).findByResidence(residenceId, new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> result) {
                devices.clear();
                devices.addAll(result);
                render();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MembersActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render() {
        adapter.setData(members, Consumption.usageByMember(devices));

        if (activeMember != null && !containsMember(activeMember.id)) activeMember = null;
        adapter.setSelectedId(activeMember == null ? -1 : activeMember.id);
        renderDetail();
    }

    private boolean containsMember(int id) {
        for (Member m : members) {
            if (m.id == id) return true;
        }
        return false;
    }

    @Override
    public void onMemberClick(Member member) {
        activeMember = activeMember != null && activeMember.id == member.id ? null : member;
        adapter.setSelectedId(activeMember == null ? -1 : activeMember.id);
        renderDetail();
    }

    @Override
    public void onAddClick() {
        if (residence == null) {
            startActivity(new Intent(this, ResidenceFormActivity.class));
            return;
        }
        Intent intent = new Intent(this, MemberFormActivity.class);
        intent.putExtra(MemberFormActivity.EXTRA_RESIDENCE_ID, residence.id);
        startActivity(intent);
    }

    private void renderDetail() {
        View detail = findViewById(R.id.memberDetail);
        detail.setVisibility(activeMember != null ? View.VISIBLE : View.GONE);
        if (activeMember == null) return;

        ((TextView) findViewById(R.id.txtDetailTitle)).setText("Dispositivos de " + activeMember.name);

        List<Device> own = new ArrayList<>();
        for (Device d : devices) {
            if (d.memberIds.contains(activeMember.id)) own.add(d);
        }
        Collections.sort(own, (a, b) -> Double.compare(Consumption.deviceShare(b).kwh, Consumption.deviceShare(a).kwh));
        findViewById(R.id.txtDetailEmpty).setVisibility(own.isEmpty() ? View.VISIBLE : View.GONE);

        double max = 1;
        for (Device d : own) max = Math.max(max, Consumption.deviceShare(d).kwh);

        LinearLayout bars = findViewById(R.id.detailBars);
        bars.removeAllViews();
        for (Device d : own) {
            Usage share = Consumption.deviceShare(d);
            View row = getLayoutInflater().inflate(R.layout.item_bar_row, bars, false);
            ((TextView) row.findViewById(R.id.txtLabel)).setText(d.displayName());
            ((TextView) row.findViewById(R.id.txtCaption)).setText(Format.kwh(share.kwh) + " · " + Format.brl(share.cost));
            ProgressBar bar = row.findViewById(R.id.bar);
            bar.setProgress((int) (share.kwh / max * 100));
            bar.setProgressTintList(ColorStateList.valueOf(getColor(R.color.green_end)));
            bars.addView(row);
        }
    }
}
