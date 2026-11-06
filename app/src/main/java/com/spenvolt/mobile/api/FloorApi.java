package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Floor;
import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.RoomKind;

import org.json.JSONObject;

import java.util.List;

/** Planta da residência: andares (/api/floors) e seções (/api/rooms). */
public class FloorApi {

    private final ApiClient api;

    public FloorApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** GET /api/floors/residence/{residenceId} → andares com as seções de cada um. */
    public void findFloors(int residenceId, ApiCallback<List<Floor>> callback) {
        api.get("/api/floors/residence/" + residenceId, Json.list(Floor::fromJson, callback));
    }

    /** POST /api/floors/residence/{residenceId} → a API escolhe o nome ("1º andar"...). */
    public void createFloor(int residenceId, ApiCallback<Floor> callback) {
        api.post("/api/floors/residence/" + residenceId, new JSONObject(), Json.object(Floor::fromJson, callback));
    }

    /** POST /api/rooms */
    public void createRoom(int floorId, RoomKind kind, int slot, String name, ApiCallback<Room> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "floorId", floorId);
        Json.put(body, "roomType", kind.name());
        Json.put(body, "slot", slot);
        Json.put(body, "name", name);
        api.post("/api/rooms", body, Json.object(Room::fromJson, callback));
    }

    /** PUT /api/rooms/{id} → renomeia a seção. */
    public void renameRoom(int roomId, String name, ApiCallback<Void> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "name", name);
        api.put("/api/rooms/" + roomId, body, Json.empty(callback));
    }

    /** DELETE /api/rooms/{id} */
    public void deleteRoom(int roomId, ApiCallback<Void> callback) {
        api.delete("/api/rooms/" + roomId, Json.empty(callback));
    }

    /** PUT /api/rooms/{id}/devices/{deviceId} → coloca o aparelho na seção (sai das outras). */
    public void assignDevice(int roomId, int deviceId, ApiCallback<Void> callback) {
        api.put("/api/rooms/" + roomId + "/devices/" + deviceId, null, Json.empty(callback));
    }

    /** DELETE /api/rooms/{id}/devices/{deviceId} */
    public void unassignDevice(int roomId, int deviceId, ApiCallback<Void> callback) {
        api.delete("/api/rooms/" + roomId + "/devices/" + deviceId, Json.empty(callback));
    }
}
