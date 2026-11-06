package com.spenvolt.mobile.ui.residence;

import android.app.Activity;
import android.widget.EditText;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Format;

/**
 * Lê e preenche os campos de view_residence_fields.xml.
 * Usado em "Criar residência" e em "Configurações".
 */
class ResidenceFields {

    private final EditText etName;
    private final EditText etAddress;
    private final EditText etTariff;
    private final EditText etAlert;
    private final EditText etKwhLimit;
    private final EditText etCostLimit;

    ResidenceFields(Activity activity) {
        etName = activity.findViewById(R.id.etName);
        etAddress = activity.findViewById(R.id.etAddress);
        etTariff = activity.findViewById(R.id.etTariff);
        etAlert = activity.findViewById(R.id.etAlert);
        etKwhLimit = activity.findViewById(R.id.etKwhLimit);
        etCostLimit = activity.findViewById(R.id.etCostLimit);
    }

    /** Preenche os campos com os dados atuais (Configurações). */
    void fill(Residence r) {
        etName.setText(r.name);
        etAddress.setText(r.address);
        etTariff.setText(Format.input(r.tariff));
        etAlert.setText(r.alertThresholdPercent == null ? "" : String.valueOf(r.alertThresholdPercent));
        etKwhLimit.setText(Format.input(r.monthlyKwhLimit));
        etCostLimit.setText(Format.input(r.monthlyCostLimit));
    }

    /**
     * Valida e copia os campos para a residência.
     * Devolve uma mensagem de erro, ou null se estiver tudo certo.
     * Campos opcionais vazios viram null (iguais ao site).
     */
    String readInto(Residence r) {
        String name = Views.text(etName);
        String address = Views.text(etAddress);
        Double tariff = Format.parseDecimal(Views.text(etTariff));
        Double alert = Format.parseDecimal(Views.text(etAlert));

        if (name.isEmpty()) return "Informe o nome da residência.";
        if (address.isEmpty()) return "Informe o endereço.";
        if (tariff == null || tariff <= 0) return "Informe a tarifa em R$ por kWh (ex.: 0.82).";
        if (alert != null && (alert < 0 || alert > 100)) return "O alerta deve ficar entre 0 e 100%.";

        r.name = name;
        r.address = address;
        r.tariff = tariff;
        r.alertThresholdPercent = alert == null ? null : alert.intValue();
        r.monthlyKwhLimit = Format.parseDecimal(Views.text(etKwhLimit));
        r.monthlyCostLimit = Format.parseDecimal(Views.text(etCostLimit));
        return null;
    }
}
