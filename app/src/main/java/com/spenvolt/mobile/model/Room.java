package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Room {

    public int id;
    public int floorId;
    public RoomKind kind;
    public int slot;
    public String name;
    public List<Integer> deviceIds = new ArrayList<>();

    public static Room fromJson(JSONObject json) throws JSONException {
        Room r = new Room();
        r.id = json.getInt("id");
        r.floorId = json.optInt("floorId");
        r.kind = RoomKind.from(json.optString("roomType"));
        r.slot = json.optInt("slot", 1);
        r.name = json.optString("name", "");
        r.deviceIds = Json.intList(json, "deviceIds");
        return r;
    }
}
