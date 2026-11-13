package com.spenvolt.mobile.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.spenvolt.mobile.ui.residence.ResidenceFormActivity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class DevicesActivity extends AppCompatActivity {

    private SessionManager session;

    private static final String[] SORT_LABELS = {"Maior consumo", "Menor consumo", "Nome (A–Z)"};
    private static final int SORT_KWH_DESC = 0;
    private static final int SORT_KWH_ASC = 1;
    private static final int SORT_NAME = 2;

    private final List<Device> devices = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();

    private String category = null;
    private int memberId = -1;
    private int sort = SORT_KWH_DESC;

    private Residence residence;
    private DeviceAdapter adapter;
    private LinearLayout categoryChips;
    private Spinner spinnerMember;
    private Spinner spinnerSort;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_devices, R.id.navDevices)) return;

        categoryChips = findViewById(R.id.categoryChips);
        spinnerMember = findViewById(R.id.spinnerMember);
        spinnerSort = findViewById(R.id.spinnerSort);
        txtStatus = findViewById(R.id.txtStatus);

        adapter = new DeviceAdapter(device -> {
            Intent intent = new Intent(this, DeviceDetailActivity.class);
            intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.id);
            startActivity(intent);
        });
        RecyclerView recycler = findViewById(R.id.recyclerDevices);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        setupSortSpinner();
        findViewById(R.id.fabAdd).setOnClickListener(v -> openNewDevice());
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
            txtStatus.setVisibility(View.GONE);
            View empty = findViewById(R.id.emptyDevices);
            empty.setVisibility(View.VISIBLE);
            ((TextView) empty.findViewById(R.id.txtEmptyTitle)).setText("Você ainda não tem residências");
            ((TextView) empty.findViewById(R.id.txtEmptyText)).setText("Cadastre uma residência para adicionar os aparelhos dela.");
            return;
        }
        ((TextView) findViewById(R.id.txtResidence)).setText(selected.name);
        loadData();
    }

    private void loadData() {
        txtStatus.setText("Carregando dispositivos…");
        txtStatus.setVisibility(View.VISIBLE);
        int residenceId = residence.id;

        new DeviceApi(this).findByResidence(residenceId, new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> result) {
                devices.clear();
                devices.addAll(result);
                txtStatus.setVisibility(View.GONE);
                renderCategories();
                applyFilters();
            }

            @Override
            public void onError(String message) {
                txtStatus.setText("Não foi possível carregar os dispositivos. Tente novamente.");
                txtStatus.setVisibility(View.VISIBLE);
            }
        });

        new MemberApi(this).findByResidence(residenceId, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> result) {
                members.clear();
                members.addAll(result);
                setupMemberSpinner();
            }

            @Override
            public void onError(String message) {
                members.clear();
                setupMemberSpinner();
            }
        });
    }

    private void renderCategories() {
        TreeSet<String> names = new TreeSet<>(Collator.getInstance(new Locale("pt", "BR")));
        for (Device d : devices) {
            if (d.category != null && !d.category.isEmpty()) names.add(d.category);
        }
        if (category != null && !names.contains(category)) category = null;

        categoryChips.removeAllViews();
        addCategoryChip("Todas", null);
        for (String name : names) {
            addCategoryChip(name, name);
        }
    }

    private void addCategoryChip(String label, @Nullable String value) {
        TextView chip = (TextView) LayoutInflater.from(categoryChips.getContext()).inflate(R.layout.item_chip, categoryChips, false);
        chip.setText(label);
        categoryChips.addView(chip);
        chip.setSelected(value == null ? category == null : value.equals(category));
        chip.setOnClickListener(v -> {
            category = value;
            renderCategories();
            applyFilters();
        });
    }

    private void setupMemberSpinner() {
        List<String> labels = new ArrayList<>();
        labels.add("Membro: todos");
        int selectedPosition = 0;
        for (int i = 0; i < members.size(); i++) {
            labels.add(members.get(i).name);
            if (members.get(i).id == memberId) selectedPosition = i + 1;
        }
        if (selectedPosition == 0) memberId = -1;

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, labels);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerMember.setOnItemSelectedListener(null);
        spinnerMember.setAdapter(spinnerAdapter);
        spinnerMember.setSelection(selectedPosition, false);
        spinnerMember.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                memberId = position == 0 ? -1 : members.get(position - 1).id;
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, SORT_LABELS);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerSort.setAdapter(spinnerAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sort = position;
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilters() {
        List<Device> visible = new ArrayList<>();
        for (Device d : devices) {
            boolean categoryOk = category == null || category.equals(d.category);
            boolean memberOk = memberId == -1 || d.memberIds.contains(memberId);
            if (categoryOk && memberOk) visible.add(d);
        }

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        if (sort == SORT_KWH_DESC) {
            Collections.sort(visible, (a, b) -> Double.compare(b.monthlyKwh, a.monthlyKwh));
        } else if (sort == SORT_KWH_ASC) {
            Collections.sort(visible, (a, b) -> Double.compare(a.monthlyKwh, b.monthlyKwh));
        } else {
            Collections.sort(visible, (a, b) -> collator.compare(a.displayName(), b.displayName()));
        }

        adapter.setDevices(visible);

        boolean filtered = !devices.isEmpty();
        View empty = findViewById(R.id.emptyDevices);
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView) empty.findViewById(R.id.txtEmptyTitle)).setText(filtered ? "Nenhum dispositivo neste filtro" : "Nenhum dispositivo cadastrado ainda");
        ((TextView) empty.findViewById(R.id.txtEmptyText)).setText(filtered ? "Ajuste os filtros para ver outros aparelhos."
                        : "Adicione os aparelhos da casa no botão + para acompanhar o consumo de cada um.");
    }

    private void openNewDevice() {
        if (residence == null) {
            startActivity(new Intent(this, ResidenceFormActivity.class));
            return;
        }
        Intent intent = new Intent(this, DeviceFormActivity.class);
        intent.putExtra(DeviceFormActivity.EXTRA_RESIDENCE_ID, residence.id);
        startActivity(intent);
    }
}
