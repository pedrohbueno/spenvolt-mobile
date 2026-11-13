package com.spenvolt.mobile.ui.device;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

public class DeviceFormActivity extends AppCompatActivity {

    private final List<Residence> residences = new ArrayList<>();
    private SessionManager session;

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
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_device_form, 0)) return;

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
            spinnerResidence.setVisibility(View.GONE);
            findViewById(R.id.txtResidence).setVisibility(View.VISIBLE);
            loadDevice();
        }
        updatePreview();
    
        new ResidenceApi(this).findAll(new ApiCallback<List<Residence>>() {
            @Override
            public void onSuccess(List<Residence> result) {
                residences.addAll(result);
                fillResidences();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DeviceFormActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isEditing() {
        return deviceId != -1;
    }

    private void fillResidences() {
        if (isEditing() || residence != null) return;

        if (residences.isEmpty()) {
            txtError.setText("Cadastre uma residência antes de adicionar dispositivos.");
            txtError.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            return;
        }

        int wantedId = getIntent().getIntExtra(EXTRA_RESIDENCE_ID, session.getSelectedResidenceId());
        int position = 0;
        for (int i = 0; i < residences.size(); i++) {
            if (residences.get(i).id == wantedId) position = i;
        }

        ArrayAdapter<Residence> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, residences);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerResidence.setAdapter(adapter);
        spinnerResidence.setSelection(position, false);
        spinnerResidence.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeResidence(residences.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                txtError.setText("Dispositivo não encontrado ou API indisponível.");
                txtError.setVisibility(View.VISIBLE);
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
                Toast.makeText(DeviceFormActivity.this, message, Toast.LENGTH_SHORT).show();
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
        findViewById(R.id.txtNoMembers).setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
        for (Member m : members) {
            TextView chip = (TextView) LayoutInflater.from(memberChips.getContext()).inflate(R.layout.item_chip, memberChips, false);
            chip.setText(m.name);
            memberChips.addView(chip);
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
        Double power = Format.parseDecimal(etPower.getText().toString().trim());
        Double hours = Format.parseDecimal(etHours.getText().toString().trim());
        Double days = Format.parseDecimal(etDays.getText().toString().trim());

        if (power == null || hours == null || days == null || residence == null) {
            txtPreview.setText("preencha potência, horas e dias");
            return;
        }
        double kwh = Consumption.monthlyKwh(power, hours, days);
        double cost = kwh * residence.tariff;
        txtPreview.setText(Format.kwh(kwh) + " · " + Format.brl(cost) + "/mês");
    }

    private void save() {
        String name = etName.getText().toString().trim();
        Double power = Format.parseDecimal(etPower.getText().toString().trim());
        Double hours = Format.parseDecimal(etHours.getText().toString().trim());
        Double days = Format.parseDecimal(etDays.getText().toString().trim());

        String error = null;
        if (name.isEmpty()) error = "Informe o nome do dispositivo.";
        else if (power == null || power < 0) error = "Informe a potência em watts.";
        else if (hours == null || hours < 0 || hours > 24) error = "Horas por dia deve ficar entre 0 e 24.";
        else if (days == null || days < 0 || days > 31) error = "Dias por mês deve ficar entre 0 e 31.";
        else if (residence == null) error = "Escolha a residência.";
        if (error != null) {
            txtError.setText(error);
            txtError.setVisibility(View.VISIBLE);
            return;
        }

        Device device = new Device();
        device.id = deviceId;
        device.name = name;
        device.brand = (etBrand.getText().toString().trim().isEmpty() ? null : etBrand.getText().toString().trim());
        device.model = (etModel.getText().toString().trim().isEmpty() ? null : etModel.getText().toString().trim());
        device.category = (etCategory.getText().toString().trim().isEmpty() ? null : etCategory.getText().toString().trim());
        device.power = power;
        device.hoursPerDay = hours;
        device.daysPerMonth = days.intValue();
        device.imageUrl = (etImageUrl.getText().toString().trim().isEmpty() ? null : etImageUrl.getText().toString().trim());
        device.residenceId = residence.id;
        device.memberIds = new ArrayList<>(selectedMemberIds);

        txtError.setVisibility(View.GONE);
        String normalText = isEditing() ? "Salvar alterações" : "Salvar dispositivo";
        btnSave.setEnabled(false);
        btnSave.setText("Salvando...");

        DeviceApi api = new DeviceApi(this);
        if (isEditing()) {
            api.update(device, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(DeviceFormActivity.this, "Dispositivo atualizado.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    btnSave.setText(normalText);
                    txtError.setText("Algo deu errado. Tente novamente.");
                    txtError.setVisibility(View.VISIBLE);
                }
            });
        } else {
            api.create(device, new ApiCallback<Device>() {
                @Override
                public void onSuccess(Device created) {
                    Toast.makeText(DeviceFormActivity.this, "Dispositivo adicionado.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    btnSave.setText(normalText);
                    txtError.setText("Algo deu errado. Tente novamente.");
                    txtError.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
