package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.MemberConsumption;

import java.util.List;

/** Membros (/api/members). */
public class MemberApi {

    private final ApiClient api;

    public MemberApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** GET /api/members?residenceId={id} */
    public void findByResidence(int residenceId, ApiCallback<List<Member>> callback) {
        api.get("/api/members?residenceId=" + residenceId, Json.list(Member::fromJson, callback));
    }

    /** GET /api/members/consumption → consumo de cada membro no mês. */
    public void findConsumption(ApiCallback<List<MemberConsumption>> callback) {
        api.get("/api/members/consumption", Json.list(MemberConsumption::fromJson, callback));
    }

    /** POST /api/members */
    public void create(Member member, ApiCallback<Member> callback) {
        api.post("/api/members", member.toJson(), Json.object(Member::fromJson, callback));
    }

    /** DELETE /api/members/{id} */
    public void delete(int id, ApiCallback<Void> callback) {
        api.delete("/api/members/" + id, Json.empty(callback));
    }
}
