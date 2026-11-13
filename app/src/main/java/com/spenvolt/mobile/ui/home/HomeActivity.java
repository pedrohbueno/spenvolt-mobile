package com.spenvolt.mobile.ui.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.DeviceApi;
import com.spenvolt.mobile.api.HistoryApi;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.History;
import com.spenvolt.mobile.model.MemberConsumption;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.ui.device.DeviceDetailActivity;
import com.spenvolt.mobile.ui.residence.ResidenceActivity;
import com.spenvolt.mobile.ui.residence.ResidenceFormActivity;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Consumption.Status;
import com.spenvolt.mobile.util.Format;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private SessionManager session;

    private static final int TOP_DEVICES = 5;

    private enum Metric { LIMIT, KWH, COST }

    private final List<Residence> residences = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private Metric metric = Metric.LIMIT;

    private LinearLayout residencesContainer;
    private LinearLayout topDevicesContainer;
    private LinearLayout memberUsageContainer;
    private LinearLayout comparisonContainer;
    private LinearLayout historyContainer;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_home, R.id.navHome)) return;
        inflater = getLayoutInflater();

        residencesContainer = findViewById(R.id.residencesContainer);
        topDevicesContainer = findViewById(R.id.topDevicesContainer);
        memberUsageContainer = findViewById(R.id.memberUsageContainer);
        comparisonContainer = findViewById(R.id.comparisonContainer);
        historyContainer = findViewById(R.id.historyContainer);

        String month = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(new Date());
        TextView txtMonth = findViewById(R.id.txtMonth);
        txtMonth.setText("Resumo de " + month);

        findViewById(R.id.btnNewResidence).setOnClickListener(v -> openNewResidence());
        findViewById(R.id.btnEmptyNewResidence).setOnClickListener(v -> openNewResidence());

        findViewById(R.id.metricLimit).setOnClickListener(v -> changeMetric(Metric.LIMIT));
        findViewById(R.id.metricKwh).setOnClickListener(v -> changeMetric(Metric.KWH));
        findViewById(R.id.metricCost).setOnClickListener(v -> changeMetric(Metric.COST));
        changeMetric(Metric.LIMIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResidences();
        loadDevices();
        loadMemberUsage();
        loadHistory();
    }

    private void loadResidences() {
        new ResidenceApi(this).findAll(new ApiCallback<List<Residence>>() {
            @Override
            public void onSuccess(List<Residence> result) {
                residences.clear();
                residences.addAll(result);
                showResidences();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResidences() {
        renderBanner();
        renderResidences();
        renderComparison();
        renderTopDevices();
    }

    private void loadDevices() {
        new DeviceApi(this).findAll(new ApiCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> result) {
                devices.clear();
                devices.addAll(result);
                renderTopDevices();
                renderComparison();
            }

            @Override
            public void onError(String message) {
                setText(R.id.txtTopDevicesStatus, message);
            }
        });
    }

    private void loadMemberUsage() {
        new MemberApi(this).findConsumption(new ApiCallback<List<MemberConsumption>>() {
            @Override
            public void onSuccess(List<MemberConsumption> result) {
                renderMemberUsage(result);
            }

            @Override
            public void onError(String message) {
                setText(R.id.txtMembersStatus, message);
            }
        });
    }

    private void loadHistory() {
        new HistoryApi(this).findAll(new ApiCallback<List<History>>() {
            @Override
            public void onSuccess(List<History> result) {
                renderHistory(result);
            }

            @Override
            public void onError(String message) {
                setText(R.id.txtHistoryStatus, message);
            }
        });
    }

    private void renderBanner() {
        double kwh = 0;
        double cost = 0;
        boolean anyExceeded = false;
        boolean anyWarning = false;
        for (Residence r : residences) {
            kwh += r.monthlyKwh;
            cost += r.monthlyCost;
            Status status = Consumption.status(r);
            anyExceeded |= status == Status.EXCEEDED;
            anyWarning |= status == Status.WARNING;
        }
        String summary = anyExceeded ? "passou do limite" : anyWarning ? "em alerta" : "no ritmo";
        String name = session.getName().isEmpty() ? "bem-vindo" : session.getName();

        String greeting = "Olá, <font color='#00C6FF'>" + name + "</font>! Sua casa está <font color='#FFD43B'>" + summary + "</font>.";
        ((TextView) findViewById(R.id.txtGreeting)).setText(Html.fromHtml(greeting, Html.FROM_HTML_MODE_LEGACY));

        setText(R.id.txtTotalKwh, Format.num(kwh));
        setText(R.id.txtTotalCost, Format.brl(cost));
        setText(R.id.txtResidenceCount, String.valueOf(residences.size()));
    }

    private void renderResidences() {
        residencesContainer.removeAllViews();
        findViewById(R.id.txtResidencesStatus).setVisibility(View.GONE);
        findViewById(R.id.emptyResidences).setVisibility(residences.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnNewResidence).setVisibility(!residences.isEmpty() ? View.VISIBLE : View.GONE);

        for (Residence r : residences) {
            View card = inflater.inflate(R.layout.item_residence_card, residencesContainer, false);
            bindResidenceCard(card, r);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, ResidenceActivity.class);
                intent.putExtra(ResidenceActivity.EXTRA_RESIDENCE_ID, r.id);
                startActivity(intent);
            });
            residencesContainer.addView(card);
        }
    }

    private void bindResidenceCard(View card, Residence r) {
        double limit = r.kwhLimit();
        Status status = Consumption.status(r);

        ((TextView) card.findViewById(R.id.txtName)).setText(r.name);
        ((TextView) card.findViewById(R.id.txtAddress)).setText(r.address);
        ((TextView) card.findViewById(R.id.txtKwh)).setText(Format.num(r.monthlyKwh));
        ((TextView) card.findViewById(R.id.txtCost)).setText(Format.brl(r.monthlyCost));
        TextView txtStatus = card.findViewById(R.id.txtStatus);
        txtStatus.setText(status.label);
        txtStatus.setBackgroundTintList(ColorStateList.valueOf(getColor(status.colorRes)));
        txtStatus.setTextColor(getColor(status.textColorRes));

        TextView txtRemaining = card.findViewById(R.id.txtRemaining);
        if (limit > 0) {
            double remaining = limit - r.monthlyKwh;
            txtRemaining.setText(Format.num(remaining));
            txtRemaining.setTextColor(ContextCompat.getColor(this, remaining < 0 ? R.color.status_exceeded : R.color.dark_blue));
        } else {
            txtRemaining.setText("—");
        }

        ProgressBar bar = card.findViewById(R.id.barUsage);
        bar.setProgress((int) Math.min(Consumption.usagePercent(r.monthlyKwh, limit), 100));
        bar.setProgressTintList(ColorStateList.valueOf(getColor(status.colorRes)));

        String limitText = limit > 0
                ? Format.num(r.monthlyKwh) + " de " + Format.kwh(limit)
                : "Sem limite de consumo definido";
        if (r.costLimit() > 0) {
            limitText += " · limite de " + Format.brl(r.costLimit());
        }
        ((TextView) card.findViewById(R.id.txtLimit)).setText(limitText);
        ((TextView) card.findViewById(R.id.txtTariff)).setText("Tarifa: " + Format.brl(r.tariff) + " por kWh");
    }

    private void openNewResidence() {
        startActivity(new Intent(this, ResidenceFormActivity.class));
    }

    private void renderTopDevices() {
        topDevicesContainer.removeAllViews();
        List<Device> sorted = new ArrayList<>(devices);
        Collections.sort(sorted, (a, b) -> Double.compare(b.monthlyKwh, a.monthlyKwh));

        if (sorted.isEmpty()) {
            setText(R.id.txtTopDevicesStatus, "Nenhum dispositivo cadastrado ainda.");
            return;
        }
        findViewById(R.id.txtTopDevicesStatus).setVisibility(View.GONE);

        for (int i = 0; i < Math.min(TOP_DEVICES, sorted.size()); i++) {
            Device d = sorted.get(i);
            if (i > 0) inflater.inflate(R.layout.view_divider, topDevicesContainer, true);

            View row = inflater.inflate(R.layout.item_top_device, topDevicesContainer, false);
            ((TextView) row.findViewById(R.id.txtRank)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.txtName)).setText(d.displayName());
            ((TextView) row.findViewById(R.id.txtResidence)).setText(residenceName(d.residenceId));
            ((TextView) row.findViewById(R.id.txtKwh)).setText(Format.kwh(d.monthlyKwh));
            ((TextView) row.findViewById(R.id.txtCost)).setText(Format.brl(d.monthlyCost));
            row.setOnClickListener(v -> {
                Intent intent = new Intent(this, DeviceDetailActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, d.id);
                startActivity(intent);
            });
            topDevicesContainer.addView(row);
        }
    }

    private String residenceName(int id) {
        for (Residence r : residences) {
            if (r.id == id) return r.name;
        }
        return "";
    }

    private void renderMemberUsage(List<MemberConsumption> members) {
        memberUsageContainer.removeAllViews();
        if (members.isEmpty()) {
            setText(R.id.txtMembersStatus, "Nenhum membro cadastrado ainda.");
            return;
        }
        findViewById(R.id.txtMembersStatus).setVisibility(View.GONE);

        double max = 1;
        for (MemberConsumption m : members) max = Math.max(max, m.kwh);

        for (MemberConsumption m : members) {
            View row = inflater.inflate(R.layout.item_bar_row, memberUsageContainer, false);
            ((TextView) row.findViewById(R.id.txtLabel)).setText(m.name);
            ((TextView) row.findViewById(R.id.txtCaption)).setText(Format.kwh(m.kwh) + " · " + Format.brl(m.cost));
            ProgressBar bar = row.findViewById(R.id.bar);
            bar.setProgress((int) (m.kwh / max * 100));
            bar.setProgressTintList(ColorStateList.valueOf(getColor(R.color.green_end)));
            memberUsageContainer.addView(row);
        }
    }

    private static class ComparisonRow {
        Residence residence;
        Status status;
        double value;
        Double limit;
        double share;
        int deviceCount;
        Device heaviest;
    }

    private void changeMetric(Metric newMetric) {
        metric = newMetric;
        findViewById(R.id.metricLimit).setSelected(metric == Metric.LIMIT);
        findViewById(R.id.metricKwh).setSelected(metric == Metric.KWH);
        findViewById(R.id.metricCost).setSelected(metric == Metric.COST);
        renderComparison();
    }

    private void renderComparison() {
        View card = findViewById(R.id.comparisonCard);
        card.setVisibility(residences.size() >= 2 ? View.VISIBLE : View.GONE);
        if (residences.size() < 2) return;

        List<ComparisonRow> rows = buildComparison();
        setText(R.id.txtInsight, comparisonInsight());

        double max = 1;
        for (ComparisonRow row : rows) {
            max = Math.max(max, Math.max(row.value, row.limit == null ? 0 : row.limit));
        }

        comparisonContainer.removeAllViews();
        for (ComparisonRow row : rows) {
            View view = inflater.inflate(R.layout.item_comparison_row, comparisonContainer, false);
            ((TextView) view.findViewById(R.id.txtName)).setText(row.residence.name);
            ((TextView) view.findViewById(R.id.txtStatus)).setText(row.status.label);
            ((TextView) view.findViewById(R.id.txtValue)).setText(
                    metric == Metric.LIMIT && row.limit == null ? "—" : formatValue(row.value));

            int barColor;
            switch (row.status) {
                case OK:       barColor = R.color.green_end; break;
                case WARNING:  barColor = R.color.yellow; break;
                case EXCEEDED: barColor = R.color.status_exceeded; break;
                default:       barColor = R.color.blue_end;
            }
            ProgressBar bar = view.findViewById(R.id.bar);
            bar.setProgress((int) (row.value / max * 100));
            bar.setProgressTintList(ColorStateList.valueOf(getColor(barColor)));

            ((TextView) view.findViewById(R.id.txtDetails)).setText(comparisonDetails(row));
            view.setOnClickListener(v -> {
                Intent intent = new Intent(this, ResidenceActivity.class);
                intent.putExtra(ResidenceActivity.EXTRA_RESIDENCE_ID, row.residence.id);
                startActivity(intent);
            });
            comparisonContainer.addView(view);
        }
    }

    private List<ComparisonRow> buildComparison() {
        double totalKwh = 0;
        for (Residence r : residences) totalKwh += r.monthlyKwh;

        List<ComparisonRow> rows = new ArrayList<>();
        for (Residence r : residences) {
            ComparisonRow row = new ComparisonRow();
            row.residence = r;
            row.share = totalKwh > 0 ? r.monthlyKwh / totalKwh * 100 : 0;

            for (Device d : devices) {
                if (d.residenceId != r.id) continue;
                row.deviceCount++;
                if (row.heaviest == null || d.monthlyKwh > row.heaviest.monthlyKwh) row.heaviest = d;
            }

            double kwhLimit = r.kwhLimit();
            double costLimit = r.costLimit();
            if (metric == Metric.LIMIT) {
                row.status = Consumption.status(r.monthlyKwh, kwhLimit, r.alertThresholdPercent);
                row.value = Consumption.usagePercent(r.monthlyKwh, kwhLimit);
                row.limit = kwhLimit > 0 ? 100.0 : null;
            } else if (metric == Metric.KWH) {
                row.status = Consumption.status(r.monthlyKwh, kwhLimit, r.alertThresholdPercent);
                row.value = r.monthlyKwh;
                row.limit = kwhLimit > 0 ? kwhLimit : null;
            } else {
                row.status = Consumption.status(r.monthlyCost, costLimit, r.alertThresholdPercent);
                row.value = r.monthlyCost;
                row.limit = costLimit > 0 ? costLimit : null;
            }
            rows.add(row);
        }

        Collections.sort(rows, (a, b) -> {
            if (metric == Metric.LIMIT && (a.limit == null) != (b.limit == null)) {
                return a.limit == null ? 1 : -1;
            }
            return Double.compare(b.value, a.value);
        });
        return rows;
    }

    private String formatValue(double value) {
        switch (metric) {
            case LIMIT: return Math.round(value) + "%";
            case KWH:   return Format.kwh(value);
            default:    return Format.brl(value);
        }
    }

    private String comparisonDetails(ComparisonRow row) {
        Residence r = row.residence;
        StringBuilder text = new StringBuilder();

        if (metric == Metric.COST) {
            text.append(r.costLimit() > 0 ? "limite de " + Format.brl(r.costLimit()) : "sem limite de custo definido");
        } else if (r.kwhLimit() <= 0) {
            text.append("sem limite de consumo definido");
        } else if (metric == Metric.LIMIT) {
            text.append(Format.num(r.monthlyKwh)).append(" de ").append(Format.kwh(r.kwhLimit()));
        } else {
            text.append("limite de ").append(Format.kwh(r.kwhLimit()));
        }

        text.append(" · ").append(Math.round(row.share)).append("% do total");
        if (row.deviceCount > 0) {
            text.append(" · ").append(row.deviceCount).append(row.deviceCount == 1 ? " dispositivo" : " dispositivos");
        }
        if (row.heaviest != null && r.monthlyKwh > 0) {
            long pct = Math.round(row.heaviest.monthlyKwh / r.monthlyKwh * 100);
            text.append(" · maior: ").append(row.heaviest.displayName()).append(" (").append(pct).append("% da casa)");
        }
        return text.toString();
    }

    private String comparisonInsight() {
        List<Residence> exceeded = new ArrayList<>();
        List<Residence> warning = new ArrayList<>();
        boolean anyLimit = false;
        double total = 0;
        Residence biggest = null;

        for (Residence r : residences) {
            Status status = Consumption.status(r);
            if (status == Status.EXCEEDED) exceeded.add(r);
            if (status == Status.WARNING) warning.add(r);
            if (r.kwhLimit() > 0) anyLimit = true;
            total += r.monthlyKwh;
            if (biggest == null || r.monthlyKwh > biggest.monthlyKwh) biggest = r;
        }

        StringBuilder text = new StringBuilder();
        if (exceeded.size() == 1) text.append(exceeded.get(0).name).append(" passou do limite de consumo. ");
        if (exceeded.size() > 1) text.append(exceeded.size()).append(" residências passaram do limite de consumo. ");
        if (warning.size() == 1) text.append(warning.get(0).name).append(" está em alerta. ");
        if (warning.size() > 1) text.append(warning.size()).append(" residências estão em alerta. ");
        if (exceeded.isEmpty() && warning.isEmpty() && anyLimit) {
            text.append("Todas as residências estão dentro do limite. ");
        }
        if (biggest != null && total > 0) {
            text.append(biggest.name).append(" concentra ")
                    .append(Math.round(biggest.monthlyKwh / total * 100)).append("% do consumo total.");
        }
        return text.toString().trim();
    }

    private void renderHistory(List<History> items) {
        historyContainer.removeAllViews();
        if (items.isEmpty()) {
            setText(R.id.txtHistoryStatus,
                    "Nenhuma alteração por enquanto. Adicionar uma residência, dispositivo ou membro aparece aqui.");
            return;
        }
        findViewById(R.id.txtHistoryStatus).setVisibility(View.GONE);

        for (int i = 0; i < items.size(); i++) {
            History h = items.get(i);
            if (i > 0) inflater.inflate(R.layout.view_divider, historyContainer, true);
            View row = inflater.inflate(R.layout.item_history, historyContainer, false);
            ((TextView) row.findViewById(R.id.txtLabel)).setText(h.label());
            ((TextView) row.findViewById(R.id.txtDate)).setText(Format.dayMonth(h.createdAt));
            historyContainer.addView(row);
        }
    }

    private void setText(int viewId, String text) {
        TextView view = findViewById(viewId);
        view.setText(text);
        view.setVisibility(View.VISIBLE);
    }
}
