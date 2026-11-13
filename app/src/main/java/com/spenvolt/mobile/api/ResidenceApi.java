package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Residence;

import java.util.List;

public class ResidenceApi {

    private final ApiClient api;

    public ResidenceApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void findAll(ApiCallback<List<Residence>> callback) {
        api.get("/api/residences", Json.list(Residence::fromJson, callback));
    }

    public void findById(int id, ApiCallback<Residence> callback) {
        api.get("/api/residences/" + id, Json.object(Residence::fromJson, callback));
    }

    public void create(Residence residence, ApiCallback<Residence> callback) {
        api.post("/api/residences", residence.toJson(), Json.object(Residence::fromJson, callback));
    }

    public void update(Residence residence, ApiCallback<Void> callback) {
        api.put("/api/residences/" + residence.id, residence.toJson(), Json.empty(callback));
    }

    public void delete(int id, ApiCallback<Void> callback) {
        api.delete("/api/residences/" + id, Json.empty(callback));
    }
}
