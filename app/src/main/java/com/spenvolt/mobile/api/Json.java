package com.spenvolt.mobile.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Funções de apoio para ler e escrever JSON.
 *
 * Detalhe importante do org.json: optString("campo") devolve o texto "null"
 * quando o valor é null. Por isso usamos os métodos daqui para campos que podem vir vazios.
 */
public final class Json {

    private Json() {
    }

    /** Converte um JSONObject em um modelo (ex.: Residence::fromJson). */
    public interface Parser<T> {
        T parse(JSONObject json) throws JSONException;
    }

    // ---------- Convertendo a resposta da API ----------

    /** Resposta que é uma lista: [ {...}, {...} ] */
    public static <T> ApiCallback<String> list(Parser<T> parser, ApiCallback<List<T>> callback) {
        return new ApiCallback<String>() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONArray array = new JSONArray(body);
                    List<T> items = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        items.add(parser.parse(array.getJSONObject(i)));
                    }
                    callback.onSuccess(items);
                } catch (JSONException e) {
                    callback.onError("Resposta inesperada da API.");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    /** Resposta que é um objeto: { ... } */
    public static <T> ApiCallback<String> object(Parser<T> parser, ApiCallback<T> callback) {
        return new ApiCallback<String>() {
            @Override
            public void onSuccess(String body) {
                try {
                    callback.onSuccess(parser.parse(new JSONObject(body)));
                } catch (JSONException e) {
                    callback.onError("Resposta inesperada da API.");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    /** Resposta sem corpo que interessa (DELETE, PUT que devolve 204...). */
    public static ApiCallback<String> empty(ApiCallback<Void> callback) {
        return new ApiCallback<String>() {
            @Override
            public void onSuccess(String body) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    // ---------- Lendo campos que podem ser null ----------

    public static String string(JSONObject json, String key) {
        return json.isNull(key) ? null : json.optString(key, null);
    }

    public static Double decimal(JSONObject json, String key) {
        return json.isNull(key) ? null : json.optDouble(key);
    }

    public static Integer integer(JSONObject json, String key) {
        return json.isNull(key) ? null : json.optInt(key);
    }

    public static List<Integer> intList(JSONObject json, String key) {
        List<Integer> list = new ArrayList<>();
        JSONArray array = json.optJSONArray(key);
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.optInt(i));
            }
        }
        return list;
    }

    // ---------- Montando o corpo das requisições ----------

    /** Coloca o valor no JSON; null vira null de verdade (e não some do objeto). */
    public static void put(JSONObject json, String key, Object value) {
        try {
            json.put(key, value == null ? JSONObject.NULL : value);
        } catch (JSONException ignored) {
            // Só acontece com números inválidos (NaN), que os formulários não deixam passar.
        }
    }
}
