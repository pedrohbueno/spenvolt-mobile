package com.spenvolt.mobile.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.ui.economize.EconomizeActivity;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

/** Detalhe de um dispositivo (devices/[id]/page.tsx do site). */
public class DeviceDetailActivity extends BaseActivity {

    public static final String EXTRA_DEVICE_ID = "device_id";

    private int deviceId;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_device_detail, NAV_NONE);
        deviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID, -1);

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceFormActivity.class);
            intent.putExtra(DeviceFormActivity.EXTRA_DEVICE_ID, deviceId);
            startActivity(intent);
        });
        findViewById(R.id.btnRemove).setOnClickListener(v -> confirmRemove());
        findViewById(R.id.btnAlternatives).setOnClickListener(v -> openAlternatives());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevice(); // recarrega ao voltar da edição
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
                Views.message(findViewById(R.id.txtError), "Dispositivo não encontrado ou API indisponível.");
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

    /** "Cozinha · CRM50 · 150 W · 24 h/dia · 30 dias/mês" */
    private String specs(Device d) {
        List<String> parts = new ArrayList<>();
        if (d.category != null) parts.add(d.category);
        if (d.model != null) parts.add(d.model);
        if (d.power != null) parts.add(Format.num(d.power) + " W");
        if (d.hoursPerDay != null) parts.add(Format.num(d.hoursPerDay) + " h/dia");
        if (d.daysPerMonth != null) parts.add(d.daysPerMonth + " dias/mês");
        return android.text.TextUtils.join(" · ", parts);
    }

    /** Busca os membros da residência e mostra só os que usam este aparelho. */
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
                Views.show(findViewById(R.id.txtNoMembers), shown == 0);
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    /** Abre o Economize já pesquisando pelo nome do aparelho. */
    private void openAlternatives() {
        if (device == null) return;
        Intent intent = new Intent(this, EconomizeActivity.class);
        intent.putExtra(EconomizeActivity.EXTRA_QUERY, device.displayName());
        startActivity(intent);
    }

    private void confirmRemove() {
        if (device == null) return;
        Views.confirm(this, "Remover dispositivo", "Remover \"" + device.displayName() + "\"?", "Remover", () ->
                new DeviceApi(this).delete(deviceId, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        toast("Dispositivo removido.");
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        toast("Não foi possível remover o dispositivo. Tente novamente.");
                    }
                }));
    }
}
