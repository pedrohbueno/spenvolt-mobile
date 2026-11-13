package com.spenvolt.mobile.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Floor {

    public int id;
    public String name;
    public int level;
    public List<Room> rooms = new ArrayList<>();

    public static Floor fromJson(JSONObject json) throws JSONException {
        Floor f = new Floor();
        f.id = json.getInt("id");
        f.name = json.optString("name", "");
        f.level = json.optInt("level");
        JSONArray array = json.optJSONArray("rooms");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                f.rooms.add(Room.fromJson(array.getJSONObject(i)));
            }
        }
        return f;
    }
}
