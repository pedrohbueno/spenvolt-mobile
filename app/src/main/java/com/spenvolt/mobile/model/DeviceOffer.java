package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONException;
import org.json.JSONObject;

/** Produto à venda encontrado em uma loja (GET /market/search). */
public class DeviceOffer {

    public String name;
    public String brand;
    public String category;
    public Double power;          // W
    public String source;         // loja
    public Double price;
    public Double originalPrice;  // preço antes do desconto
    public String deviceUrl;      // link para a loja

    public static DeviceOffer fromJson(JSONObject json) throws JSONException {
        DeviceOffer o = new DeviceOffer();
        o.name = json.optString("name", "");
        o.brand = Json.string(json, "brand");
        o.category = Json.string(json, "category");
        o.power = Json.decimal(json, "power");
        o.source = Json.string(json, "source");
        o.price = Json.decimal(json, "price");
        o.originalPrice = Json.decimal(json, "originalPrice");
        o.deviceUrl = Json.string(json, "deviceUrl");
        return o;
    }

    /** Desconto em %, ou 0 quando não há preço original maior. */
    public int discountPercent() {
        if (price == null || originalPrice == null || originalPrice <= price) return 0;
        return (int) Math.round((1 - price / originalPrice) * 100);
    }
}
