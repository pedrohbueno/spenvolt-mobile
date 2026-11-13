package com.spenvolt.mobile.api;

import android.content.Context;

import org.json.JSONObject;

public class UserApi {

    private final ApiClient api;

    public UserApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void changePassword(String currentPassword, String newPassword, ApiCallback<Void> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "currentPassword", currentPassword);
        Json.put(body, "newPassword", newPassword);
        api.put("/api/users/me/password", body, Json.empty(callback));
    }
}
