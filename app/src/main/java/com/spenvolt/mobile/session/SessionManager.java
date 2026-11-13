package com.spenvolt.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SessionManager {

    private static final String PREFS = "spenvolt_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_RESIDENCE_ID = "residence_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveLogin(String token) {
        String name = "";
        String email = "";
        long expiresAt = 0;

        try {
            String payload = token.split("\\.")[1];
            byte[] bytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            JSONObject json = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
            name = json.optString("name", "");
            email = json.optString("sub", "");
            expiresAt = json.optLong("exp", 0) * 1000;
        } catch (Exception ignored) {
        }

        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && System.currentTimeMillis() < prefs.getLong(KEY_EXPIRES_AT, 0);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public int getSelectedResidenceId() {
        return prefs.getInt(KEY_RESIDENCE_ID, -1);
    }

    public void setSelectedResidenceId(int id) {
        prefs.edit().putInt(KEY_RESIDENCE_ID, id).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
