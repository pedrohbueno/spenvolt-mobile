package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class Residence {

    public int id;
    public String name;
    public String address;
    public double tariff;
    public double monthlyKwh;
    public double monthlyCost;
    public Double monthlyKwhLimit;
    public Double monthlyCostLimit;
    public Integer alertThresholdPercent;

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

    public double kwhLimit() {
        return monthlyKwhLimit == null ? 0 : monthlyKwhLimit;
    }

    public double costLimit() {
        return monthlyCostLimit == null ? 0 : monthlyCostLimit;
    }

    @Override
    public String toString() {
        return name;
    }
}
