package com.spenvolt.mobile.model;

import com.spenvolt.mobile.api.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class Member {

    public int id;
    public String name;
    public String email;
    public String photoUrl;
    public int residenceId;

    public static Member fromJson(JSONObject json) throws JSONException {
        Member m = new Member();
        m.id = json.getInt("id");
        m.name = json.optString("name", "");
        m.email = json.optString("email", "");
        m.photoUrl = Json.string(json, "photoUrl");
        m.residenceId = json.optInt("residenceId");
        return m;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        Json.put(json, "name", name);
        Json.put(json, "email", email);
        Json.put(json, "photoUrl", photoUrl);
        Json.put(json, "residenceId", residenceId);
        return json;
    }

    @Override
    public String toString() {
        return name;
    }
}
