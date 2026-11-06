package com.spenvolt.mobile.api;

import android.content.Context;

import org.json.JSONObject;

/** Login e cadastro (/api/auth). São as únicas rotas que não precisam de token. */
public class AuthApi {

    private final ApiClient api;

    public AuthApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    /** POST /api/auth/login → devolve o token JWT. */
    public void login(String email, String password, ApiCallback<String> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "email", email);
        Json.put(body, "password", password);
        api.post("/api/auth/login", body, Json.object(json -> json.getString("token"), callback));
    }

    /** POST /api/auth/register */
    public void register(String name, String email, String password, ApiCallback<Void> callback) {
        JSONObject body = new JSONObject();
        Json.put(body, "name", name);
        Json.put(body, "email", email);
        Json.put(body, "password", password);
        api.post("/api/auth/register", body, Json.empty(callback));
    }
}
