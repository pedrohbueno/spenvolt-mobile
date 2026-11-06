package com.spenvolt.mobile.api;

import android.content.Context;
import android.net.Uri;

import com.spenvolt.mobile.model.DeviceOffer;

import java.util.List;

/** Ofertas de aparelhos nas lojas. Atenção: esta rota fica fora de /api. */
public class MarketApi {

    private final ApiClient api;

    public MarketApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** GET /market/search?query=geladeira */
    public void search(String query, ApiCallback<List<DeviceOffer>> callback) {
        api.get("/market/search?query=" + Uri.encode(query), Json.list(DeviceOffer::fromJson, callback));
    }
}
