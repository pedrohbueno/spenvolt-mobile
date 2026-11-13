package com.spenvolt.mobile.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailActivity extends AppCompatActivity {

    private SessionManager session;

    public static final String EXTRA_DEVICE_ID = "device_id";

    private int deviceId;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_device_detail, 0)) return;
        deviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID, -1);

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceFormActivity.class);
            intent.putExtra(DeviceFormActivity.EXTRA_DEVICE_ID, deviceId);
            startActivity(intent);
        });
        findViewById(R.id.btnRemove).setOnClickListener(v -> confirmRemove());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevice();
    }

    private void loadDevice() {
        new DeviceApi(this).findById(deviceId, new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                device = result;
                render();
                loadMembers();
            }

            @Override
            public void onError(String message) {
                ((TextView) findViewById(R.id.txtError)).setText("Dispositivo não encontrado ou API indisponível.");
                findViewById(R.id.txtError).setVisibility(View.VISIBLE);
            }
        });
    }

    private void render() {
        ((TextView) findViewById(R.id.txtInitial)).setText(Format.initial(device.displayName()));
        ((TextView) findViewById(R.id.txtTitle)).setText(device.fullName());
        ((TextView) findViewById(R.id.txtSpecs)).setText(specs(device));
        ((TextView) findViewById(R.id.txtKwh)).setText(Format.kwh(device.monthlyKwh));
        ((TextView) findViewById(R.id.txtCost)).setText(Format.brl(device.monthlyCost));
    }

    private String specs(Device d) {
        List<String> parts = new ArrayList<>();
        if (d.category != null) parts.add(d.category);
        if (d.model != null) parts.add(d.model);
        if (d.power != null) parts.add(Format.num(d.power) + " W");
        if (d.hoursPerDay != null) parts.add(Format.num(d.hoursPerDay) + " h/dia");
        if (d.daysPerMonth != null) parts.add(d.daysPerMonth + " dias/mês");
        return android.text.TextUtils.join(" · ", parts);
    }

    private void loadMembers() {
        new MemberApi(this).findByResidence(device.residenceId, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> members) {
                ChipGroup container = findViewById(R.id.memberChips);
                container.removeAllViews();
                int shown = 0;
                for (Member m : members) {
                    if (!device.memberIds.contains(m.id)) continue;
                    View chip = getLayoutInflater().inflate(R.layout.item_member_chip, container, false);
                    ((TextView) chip.findViewById(R.id.txtAvatar)).setText(Format.initial(m.name));
                    ((TextView) chip.findViewById(R.id.txtName)).setText(m.name);
                    container.addView(chip);
                    shown++;
                }
                findViewById(R.id.txtNoMembers).setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DeviceDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmRemove() {
        if (device == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover dispositivo")
                .setMessage("Remover \"" + device.displayName() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialog, which) ->
                new DeviceApi(this).delete(deviceId, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(DeviceDetailActivity.this, "Dispositivo removido.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(DeviceDetailActivity.this, "Não foi possível remover o dispositivo. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                }))
                .show();
    }
}
