package com.spenvolt.mobile.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class History {

    public int id;
    public String type;
    public String entityName;
    public String createdAt;

    public static History fromJson(JSONObject json) throws JSONException {
        History h = new History();
        h.id = json.getInt("id");
        h.type = json.optString("type", "");
        h.entityName = json.optString("entityName", "");
        h.createdAt = readDate(json);
        return h;
    }

    private static String readDate(JSONObject json) {
        JSONArray parts = json.optJSONArray("createdAt");
        if (parts != null && parts.length() >= 3) {
            return String.format(Locale.ROOT, "%04d-%02d-%02d", parts.optInt(0), parts.optInt(1), parts.optInt(2));
        }
        return json.optString("createdAt", "");
    }

    public String label() {
        String[] parts = type.split("_");
        String entity = parts.length > 0 ? parts[0] : "";
        String action = parts.length > 1 ? parts[1] : "";

        String actionLabel;
        switch (action) {
            case "ADDED":   actionLabel = "Adicionado:"; break;
            case "UPDATED": actionLabel = "Atualizada:"; break;
            case "REMOVED": actionLabel = "Removido:"; break;
            default:        actionLabel = action;
        }

        String entityLabel;
        switch (entity) {
            case "DEVICE":    entityLabel = "Dispositivo"; break;
            case "MEMBER":    entityLabel = "Membro"; break;
            case "RESIDENCE": entityLabel = "Residência"; break;
            default:          entityLabel = entity;
        }

        return actionLabel + " " + entityLabel + " - " + entityName;
    }
}
