package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Residence;

import java.util.List;

/** Residências (/api/residences). */
public class ResidenceApi {

    private final ApiClient api;

    public ResidenceApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** GET /api/residences → residências do usuário logado. */
    public void findAll(ApiCallback<List<Residence>> callback) {
        api.get("/api/residences", Json.list(Residence::fromJson, callback));
    }

    /** GET /api/residences/{id} */
    public void findById(int id, ApiCallback<Residence> callback) {
        api.get("/api/residences/" + id, Json.object(Residence::fromJson, callback));
    }

    /** POST /api/residences */
    public void create(Residence residence, ApiCallback<Residence> callback) {
        api.post("/api/residences", residence.toJson(), Json.object(Residence::fromJson, callback));
    }

    /** PUT /api/residences/{id} */
    public void update(Residence residence, ApiCallback<Void> callback) {
        api.put("/api/residences/" + residence.id, residence.toJson(), Json.empty(callback));
    }

    /** DELETE /api/residences/{id} */
    public void delete(int id, ApiCallback<Void> callback) {
        api.delete("/api/residences/" + id, Json.empty(callback));
    }
}
