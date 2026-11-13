package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.MemberConsumption;

import java.util.List;

public class MemberApi {

    private final ApiClient api;

    public MemberApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void findByResidence(int residenceId, ApiCallback<List<Member>> callback) {
        api.get("/api/members?residenceId=" + residenceId, Json.list(Member::fromJson, callback));
    }

    public void findConsumption(ApiCallback<List<MemberConsumption>> callback) {
        api.get("/api/members/consumption", Json.list(MemberConsumption::fromJson, callback));
    }

    public void create(Member member, ApiCallback<Member> callback) {
        api.post("/api/members", member.toJson(), Json.object(Member::fromJson, callback));
    }

    public void delete(int id, ApiCallback<Void> callback) {
        api.delete("/api/members/" + id, Json.empty(callback));
    }
}
