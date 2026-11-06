package com.spenvolt.mobile.ui.economize;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.MarketApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.DeviceOffer;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Format;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Economize: busca ofertas nas lojas (GET /market/search) e estima
 * quanto o usuário economizaria trocando o aparelho que ele já tem.
 */
public class EconomizeActivity extends BaseActivity implements OfferAdapter.Listener {

    public static final String EXTRA_QUERY = "query";

    private static final String[] QUICK_SEARCHES = {"Geladeira", "Ar-condicionado", "Chuveiro", "TV", "Máquina de lavar"};

    private final List<Device> devices = new ArrayList<>(); // aparelhos da residência selecionada
    private Residence residence;
    private String query = "";
    private Device mine;  // aparelho do usuário que combina com a busca

    private EditText etSearch;
    private LinearLayout quickChips;
    private TextView txtStatus;
    private OfferAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_economize, NAV_ECONOMIZE);

        etSearch = findViewById(R.id.etSearch);
        quickChips = findViewById(R.id.quickChips);
        txtStatus = findViewById(R.id.txtStatus);

        adapter = new OfferAdapter(this);
        RecyclerView recycler = findViewById(R.id.recyclerOffers);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(Views.text(etSearch));
                return true;
            }
            return false;
        });
        renderQuickChips();

        // Vindo do detalhe do aparelho, já abre pesquisando
        String initial = getIntent().getStringExtra(EXTRA_QUERY);
        if (initial != null) {
            etSearch.setText(initial);
            search(initial);
        }
    }

    // ======================= Residência =======================

    /** Os aparelhos da residência servem para comparar com as ofertas. */
    @Override
    protected void onResidencesLoaded(@Nullable Residence selected) {
        if (selected != null && residence != null && selected.id == residence.id) return; // já carregado
        residence = selected;
        loadDevices();
    }

    @Override
    protected void onResidenceChanged(Residence selected) {
        residence = selected;
        loadDevices();
    }

    private void loadDevices() {
        devices.clear();
        if (residence == null) return;
        new DeviceApi(this).findByResidence(residence.id, new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> result) {
                devices.clear();
                devices.addAll(result);
                if (!query.isEmpty()) search(query); // refaz a comparação
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    // ======================= Busca =======================

    private void renderQuickChips() {
        quickChips.removeAllViews();
        for (String label : QUICK_SEARCHES) {
            TextView chip = Views.addChip(quickChips, label);
            chip.setSelected(label.equalsIgnoreCase(query));
            chip.setOnClickListener(v -> {
                etSearch.setText(label);
                search(label);
            });
        }
    }

    private void search(String text) {
        query = text.trim();
        renderQuickChips();
        hideKeyboard();
        if (query.isEmpty()) return;

        Views.message(txtStatus, "Buscando ofertas…");
        new MarketApi(this).search(query, new ApiCallback<List<DeviceOffer>>() {
            @Override
            public void onSuccess(List<DeviceOffer> offers) {
                mine = Savings.findMine(query, devices);
                double tariff = residence == null ? 0 : residence.tariff;
                adapter.setOffers(offers, mine, tariff);

                if (offers.isEmpty()) {
                    Views.message(txtStatus, "Nenhuma oferta encontrada para \"" + query + "\".");
                } else if (mine != null) {
                    Views.message(txtStatus, "Comparando com o seu " + mine.displayName()
                            + " (" + Format.kwh(mine.monthlyKwh) + "/mês).");
                } else {
                    Views.message(txtStatus, offers.size() + " ofertas encontradas.");
                }
            }

            @Override
            public void onError(String message) {
                adapter.setOffers(new ArrayList<>(), null, 0);
                Views.message(txtStatus, "Não foi possível buscar ofertas. Tente novamente.");
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    // ======================= Ações dos cards =======================

    /** Mostra lado a lado o aparelho atual e a oferta. */
    @Override
    public void onCompare(DeviceOffer offer) {
        if (mine == null) {
            toast("Cadastre um aparelho parecido para comparar.");
            return;
        }
        double tariff = residence == null ? 0 : residence.tariff;
        Savings savings = Savings.estimate(offer, mine, tariff);

        StringBuilder text = new StringBuilder();
        text.append("Seu aparelho: ").append(mine.fullName()).append("\n")
                .append(mine.power == null ? "—" : Format.num(mine.power) + " W")
                .append(" · ").append(Format.kwh(mine.monthlyKwh))
                .append(" · ").append(Format.brl(mine.monthlyCost)).append("/mês\n\n");

        text.append("Esta oferta: ").append(offer.name).append("\n");
        if (savings == null) {
            text.append("Sem dados de potência para estimar o consumo.");
        } else {
            text.append(Format.num(offer.power)).append(" W")
                    .append(" · ").append(Format.kwh(savings.offerKwh))
                    .append(" · ").append(Format.brl(savings.offerCost)).append("/mês\n\n")
                    .append(Savings.text(savings, mine));
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Comparar com o meu")
                .setMessage(text.toString())
                .setPositiveButton("Fechar", null)
                .show();
    }

    /** Abre a página do produto no navegador. */
    @Override
    public void onOpenStore(DeviceOffer offer) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(offer.deviceUrl)));
        } catch (ActivityNotFoundException e) {
            toast("Nenhum navegador encontrado.");
        }
    }
}
