package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Aparelho de uma residência (DeviceResponseDTO da API). */
public class Device {

    public int id;
    public String name;
    public String brand;
    public String model;
    public String category;
    public Double power;          // W
    public Double hoursPerDay;
    public Integer daysPerMonth;
    public String imageUrl;
    public int residenceId;
    public List<Integer> memberIds = new ArrayList<>();
    public double monthlyKwh;     // calculado pela API
    public double monthlyCost;    // calculado pela API

    public static Device fromJson(JSONObject json) throws JSONException {
        Device d = new Device();
        d.id = json.getInt("id");
        d.name = Json.string(json, "name");
        d.brand = Json.string(json, "brand");
        d.model = Json.string(json, "model");
        d.category = Json.string(json, "category");
        d.power = Json.decimal(json, "power");
        d.hoursPerDay = Json.decimal(json, "hoursPerDay");
        d.daysPerMonth = Json.integer(json, "daysPerMonth");
        d.imageUrl = Json.string(json, "imageUrl");
        d.residenceId = json.optInt("residenceId");
        d.memberIds = Json.intList(json, "memberIds");
        d.monthlyKwh = json.optDouble("monthlyKwh", 0);
        d.monthlyCost = json.optDouble("monthlyCost", 0);
        return d;
    }

    /** Corpo do POST/PUT /api/devices. O dono vem do token; consumo e custo a API calcula. */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        Json.put(json, "name", name);
        Json.put(json, "brand", brand);
        Json.put(json, "model", model);
        Json.put(json, "category", category);
        Json.put(json, "power", power);
        Json.put(json, "hoursPerDay", hoursPerDay);
        Json.put(json, "daysPerMonth", daysPerMonth);
        Json.put(json, "imageUrl", imageUrl);
        Json.put(json, "residenceId", residenceId);
        Json.put(json, "memberIds", new JSONArray(memberIds));
        return json;
    }

    public String displayName() {
        return name == null || name.isEmpty() ? "Sem nome" : name;
    }

    /** "Geladeira Brastemp" */
    public String fullName() {
        return brand == null || brand.isEmpty() ? displayName() : displayName() + " " + brand;
    }
}
