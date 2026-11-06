package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Device;

import java.util.List;

/** Dispositivos (/api/devices). */
public class DeviceApi {

    private final ApiClient api;

    public DeviceApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** GET /api/devices → todos os aparelhos do usuário (todas as residências). */
    public void findAll(ApiCallback<List<Device>> callback) {
        api.get("/api/devices", Json.list(Device::fromJson, callback));
    }

    /** GET /api/devices/residence/{residenceId} */
    public void findByResidence(int residenceId, ApiCallback<List<Device>> callback) {
        api.get("/api/devices/residence/" + residenceId, Json.list(Device::fromJson, callback));
    }

    /** GET /api/devices/{id} */
    public void findById(int id, ApiCallback<Device> callback) {
        api.get("/api/devices/" + id, Json.object(Device::fromJson, callback));
    }

    /** POST /api/devices */
    public void create(Device device, ApiCallback<Device> callback) {
        api.post("/api/devices", device.toJson(), Json.object(Device::fromJson, callback));
    }

    /** PUT /api/devices/{id} */
    public void update(Device device, ApiCallback<Void> callback) {
        api.put("/api/devices/" + device.id, device.toJson(), Json.empty(callback));
    }

    /** DELETE /api/devices/{id} */
    public void delete(int id, ApiCallback<Void> callback) {
        api.delete("/api/devices/" + id, Json.empty(callback));
    }
}
