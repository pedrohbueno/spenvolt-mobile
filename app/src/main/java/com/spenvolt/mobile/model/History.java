package com.spenvolt.mobile.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/** Item do histórico de alterações (HistoryResponseDTO da API). */
public class History {

    public int id;
    public String type;        // ex.: "DEVICE_ADDED", "RESIDENCE_UPDATED"
    public String entityName;
    public String createdAt;   // ex.: "2026-10-27T14:30:00"

    public static History fromJson(JSONObject json) throws JSONException {
        History h = new History();
        h.id = json.getInt("id");
        h.type = json.optString("type", "");
        h.entityName = json.optString("entityName", "");
        h.createdAt = readDate(json);
        return h;
    }

    /**
     * A data costuma vir como texto ("2026-10-27T14:30:00"), mas dependendo da
     * configuração do Jackson na API pode vir como lista ([2026, 10, 27, 14, 30]).
     */
    private static String readDate(JSONObject json) {
        JSONArray parts = json.optJSONArray("createdAt");
        if (parts != null && parts.length() >= 3) {
            return String.format(Locale.ROOT, "%04d-%02d-%02d", parts.optInt(0), parts.optInt(1), parts.optInt(2));
        }
        return json.optString("createdAt", "");
    }

    /** "Adicionado: Dispositivo - Geladeira" (mesmo texto do site). */
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
