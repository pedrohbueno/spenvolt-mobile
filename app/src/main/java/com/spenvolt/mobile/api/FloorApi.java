package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Floor;
import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.RoomKind;

import org.json.JSONObject;

import java.util.List;

public class FloorApi {

    private final ApiClient api;

    public FloorApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void findFloors(int residenceId, ApiCallback<List<Floor>> callback) {
        api.get("/api/floors/residence/" + residenceId, Json.list(Floor::fromJson, callback));
    }

    public void createFloor(int residenceId, ApiCallback<Floor> callback) {
        api.post("/api/floors/residence/" + residenceId, new JSONObject(), Json.object(Floor::fromJson, callback));
    }

    public void createRoom(int floorId, RoomKind kind, int slot, String name, ApiCallback<Room> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "floorId", floorId);
        Json.put(body, "roomType", kind.name());
        Json.put(body, "slot", slot);
        Json.put(body, "name", name);
        api.post("/api/rooms", body, Json.object(Room::fromJson, callback));
    }

    public void renameRoom(int roomId, String name, ApiCallback<Void> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "name", name);
        api.put("/api/rooms/" + roomId, body, Json.empty(callback));
    }

    public void deleteRoom(int roomId, ApiCallback<Void> callback) {
        api.delete("/api/rooms/" + roomId, Json.empty(callback));
    }

    public void assignDevice(int roomId, int deviceId, ApiCallback<Void> callback) {
        api.put("/api/rooms/" + roomId + "/devices/" + deviceId, null, Json.empty(callback));
    }

    public void unassignDevice(int roomId, int deviceId, ApiCallback<Void> callback) {
        api.delete("/api/rooms/" + roomId + "/devices/" + deviceId, Json.empty(callback));
    }
}
