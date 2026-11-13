package com.spenvolt.mobile.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MemberConsumption {

    public int memberId;
    public String name;
    public int residenceId;
    public double kwh;
    public double cost;

    public static MemberConsumption fromJson(JSONObject json) throws JSONException {
        MemberConsumption m = new MemberConsumption();
        m.memberId = json.getInt("memberId");
        m.name = json.optString("name", "");
        m.residenceId = json.optInt("residenceId");
        m.kwh = json.optDouble("kwh", 0);
        m.cost = json.optDouble("cost", 0);
        return m;
    }
}
