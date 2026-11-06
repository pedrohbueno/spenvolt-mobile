package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONException;
import org.json.JSONObject;

/** Residência do usuário (ResidenceResponseDTO da API). */
public class Residence {

    public int id;
    public String name;
    public String address;
    public double tariff;                  // R$ por kWh
    public double monthlyKwh;              // calculado pela API
    public double monthlyCost;             // calculado pela API
    public Double monthlyKwhLimit;         // pode ser null (sem limite)
    public Double monthlyCostLimit;        // pode ser null
    public Integer alertThresholdPercent;  // pode ser null (o padrão é 80%)

    public static Residence fromJson(JSONObject json) throws JSONException {
        Residence r = new Residence();
        r.id = json.getInt("id");
        r.name = json.optString("name", "");
        r.address = json.optString("address", "");
        r.tariff = json.optDouble("tariff", 0);
        r.monthlyKwh = json.optDouble("monthlyKwh", 0);
        r.monthlyCost = json.optDouble("monthlyCost", 0);
        r.monthlyKwhLimit = Json.decimal(json, "monthlyKwhLimit");
        r.monthlyCostLimit = Json.decimal(json, "monthlyCostLimit");
        r.alertThresholdPercent = Json.integer(json, "alertThresholdPercent");
        return r;
    }

    /** Corpo do POST/PUT /api/residences. */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        Json.put(json, "name", name);
        Json.put(json, "address", address);
        Json.put(json, "tariff", tariff);
        Json.put(json, "monthlyKwhLimit", monthlyKwhLimit);
        Json.put(json, "monthlyCostLimit", monthlyCostLimit);
        Json.put(json, "alertThresholdPercent", alertThresholdPercent);
        return json;
    }

    /** Limite em kWh, ou 0 quando não foi definido. */
    public double kwhLimit() {
        return monthlyKwhLimit == null ? 0 : monthlyKwhLimit;
    }

    public double costLimit() {
        return monthlyCostLimit == null ? 0 : monthlyCostLimit;
    }

    /** Usado pelo Spinner para mostrar o nome. */
    @Override
    public String toString() {
        return name;
    }
}
