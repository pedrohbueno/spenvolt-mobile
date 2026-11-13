package com.spenvolt.mobile.ui.device;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.SimpleSelection;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

public class DeviceFormActivity extends BaseActivity {

    public static final String EXTRA_DEVICE_ID = "device_id";
    public static final String EXTRA_RESIDENCE_ID = "residence_id";

    private int deviceId;
    private Residence residence;
    private final List<Member> members = new ArrayList<>();
    private final List<Integer> selectedMemberIds = new ArrayList<>();

    private EditText etName, etBrand, etModel, etCategory, etPower, etHours, etDays, etImageUrl;
    private Spinner spinnerResidence;
    private ChipGroup memberChips;
    private TextView txtPreview;
    private TextView txtError;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_device_form, NAV_NONE);

        deviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID, -1);

        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etCategory = findViewById(R.id.etCategory);
        etPower = findViewById(R.id.etPower);
        etHours = findViewById(R.id.etHours);
        etDays = findViewById(R.id.etDays);
        etImageUrl = findViewById(R.id.etImageUrl);
        spinnerResidence = findViewById(R.id.spinnerResidence);
        memberChips = findViewById(R.id.memberChips);
        txtPreview = findViewById(R.id.txtPreview);
        txtError = findViewById(R.id.txtError);
        btnSave = findViewById(R.id.btnSave);

        TextWatcher previewWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { updatePreview(); }
        };
        etPower.addTextChangedListener(previewWatcher);
        etHours.addTextChangedListener(previewWatcher);
        etDays.addTextChangedListener(previewWatcher);

        btnSave.setOnClickListener(v -> save());

        if (isEditing()) {
            ((TextView) findViewById(R.id.txtTitle)).setText("Editar dispositivo");
            btnSave.setText("Salvar alterações");
            Views.show(spinnerResidence, false);
            Views.show(findViewById(R.id.txtResidence), true);
            loadDevice();
        }
        updatePreview();
    }

    private boolean isEditing() {
        return deviceId != -1;
    }

    @Override
    protected void onResidencesLoaded(@Nullable Residence selected) {
        if (isEditing() || residence != null) return;

        if (residences.isEmpty()) {
            Views.message(txtError, "Cadastre uma residência antes de adicionar dispositivos.");
            btnSave.setEnabled(false);
            return;
        }

        int wantedId = getIntent().getIntExtra(EXTRA_RESIDENCE_ID, selected == null ? -1 : selected.id);
        int position = 0;
        for (int i = 0; i < residences.size(); i++) {
            if (residences.get(i).id == wantedId) position = i;
        }

        ArrayAdapter<Residence> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, residences);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerResidence.setAdapter(adapter);
        spinnerResidence.setSelection(position, false);
        spinnerResidence.setOnItemSelectedListener(new SimpleSelection(index -> changeResidence(residences.get(index))));
        changeResidence(residences.get(position));
    }

    private void changeResidence(Residence newResidence) {
        if (residence != null && residence.id == newResidence.id) return;
        residence = newResidence;
        updatePreview();
        loadMembers();
    }

    private void loadDevice() {
        new DeviceApi(this).findById(deviceId, new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device d) {
                etName.setText(d.name);
                etBrand.setText(d.brand);
                etModel.setText(d.model);
                etCategory.setText(d.category);
                etPower.setText(Format.input(d.power));
                etHours.setText(Format.input(d.hoursPerDay));
                etDays.setText(d.daysPerMonth == null ? "" : String.valueOf(d.daysPerMonth));
                etImageUrl.setText(d.imageUrl);
                selectedMemberIds.clear();
                selectedMemberIds.addAll(d.memberIds);
                loadResidence(d.residenceId);
            }

            @Override
            public void onError(String message) {
                Views.message(txtError, "Dispositivo não encontrado ou API indisponível.");
            }
        });
    }

    private void loadResidence(int residenceId) {
        new ResidenceApi(this).findById(residenceId, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence result) {
                residence = result;
                ((TextView) findViewById(R.id.txtResidence)).setText(result.name);
                updatePreview();
                loadMembers();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void loadMembers() {
        new MemberApi(this).findByResidence(residence.id, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> result) {
                members.clear();
                members.addAll(result);
                renderMemberChips();
            }

            @Override
            public void onError(String message) {
                members.clear();
                renderMemberChips();
            }
        });
    }

    private void renderMemberChips() {
        List<Integer> valid = new ArrayList<>();
        for (Member m : members) {
            if (selectedMemberIds.contains(m.id)) valid.add(m.id);
        }
        selectedMemberIds.clear();
        selectedMemberIds.addAll(valid);

        memberChips.removeAllViews();
        Views.show(findViewById(R.id.txtNoMembers), members.isEmpty());
        for (Member m : members) {
            TextView chip = Views.addChip(memberChips, m.name);
            chip.setSelected(selectedMemberIds.contains(m.id));
            chip.setOnClickListener(v -> {
                if (selectedMemberIds.contains(m.id)) {
                    selectedMemberIds.remove(Integer.valueOf(m.id));
                } else {
                    selectedMemberIds.add(m.id);
                }
                chip.setSelected(selectedMemberIds.contains(m.id));
            });
        }
    }

    private void updatePreview() {
        Double power = Format.parseDecimal(Views.text(etPower));
        Double hours = Format.parseDecimal(Views.text(etHours));
        Double days = Format.parseDecimal(Views.text(etDays));

        if (power == null || hours == null || days == null || residence == null) {
            txtPreview.setText("preencha potência, horas e dias");
            return;
        }
        double kwh = Consumption.monthlyKwh(power, hours, days);
        double cost = kwh * residence.tariff;
        txtPreview.setText(Format.kwh(kwh) + " · " + Format.brl(cost) + "/mês");
    }

    private void save() {
        String name = Views.text(etName);
        Double power = Format.parseDecimal(Views.text(etPower));
        Double hours = Format.parseDecimal(Views.text(etHours));
        Double days = Format.parseDecimal(Views.text(etDays));

        String error = null;
        if (name.isEmpty()) error = "Informe o nome do dispositivo.";
        else if (power == null || power < 0) error = "Informe a potência em watts.";
        else if (hours == null || hours < 0 || hours > 24) error = "Horas por dia deve ficar entre 0 e 24.";
        else if (days == null || days < 0 || days > 31) error = "Dias por mês deve ficar entre 0 e 31.";
        else if (residence == null) error = "Escolha a residência.";
        if (error != null) {
            Views.message(txtError, error);
            return;
        }

        Device device = new Device();
        device.id = deviceId;
        device.name = name;
        device.brand = Views.textOrNull(etBrand);
        device.model = Views.textOrNull(etModel);
        device.category = Views.textOrNull(etCategory);
        device.power = power;
        device.hoursPerDay = hours;
        device.daysPerMonth = days.intValue();
        device.imageUrl = Views.textOrNull(etImageUrl);
        device.residenceId = residence.id;
        device.memberIds = new ArrayList<>(selectedMemberIds);

        Views.message(txtError, null);
        String normalText = isEditing() ? "Salvar alterações" : "Salvar dispositivo";
        Views.loading(btnSave, true, "Salvando...", normalText);

        DeviceApi api = new DeviceApi(this);
        if (isEditing()) {
            api.update(device, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    toast("Dispositivo atualizado.");
                    finish();
                }

                @Override
                public void onError(String message) {
                    Views.loading(btnSave, false, "Salvando...", normalText);
                    Views.message(txtError, "Algo deu errado. Tente novamente.");
                }
            });
        } else {
            api.create(device, new ApiCallback<Device>() {
                @Override
                public void onSuccess(Device created) {
                    toast("Dispositivo adicionado.");
                    finish();
                }

                @Override
                public void onError(String message) {
                    Views.loading(btnSave, false, "Salvando...", normalText);
                    Views.message(txtError, "Algo deu errado. Tente novamente.");
                }
            });
        }
    }
}
