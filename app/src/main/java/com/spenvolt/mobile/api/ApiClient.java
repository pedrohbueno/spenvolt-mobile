package com.spenvolt.mobile.api;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spenvolt.mobile.BuildConfig;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.auth.LoginActivity;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Faz as requisições HTTP para a spenvolt-api usando Volley.
 *
 * - Monta a URL com BuildConfig.API_URL (definido no app/build.gradle.kts).
 * - Envia o token JWT no cabeçalho Authorization.
 * - Devolve o corpo da resposta como texto; as classes *Api convertem para os modelos.
 * - Se a API responder 401 (token vencido), volta para a tela de login.
 */
public class ApiClient {

    private static final int TIMEOUT_MS = 15_000;

    private static ApiClient instance;

    private final Context appContext;
    private final RequestQueue queue;
    private final SessionManager session;

    private ApiClient(Context context) {
        appContext = context.getApplicationContext();
        queue = Volley.newRequestQueue(appContext);
        session = new SessionManager(appContext);
    }

    /** Uma única fila de requisições para o app inteiro. */
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public void get(String path, ApiCallback<String> callback) {
        send(Request.Method.GET, path, null, callback);
    }

    public void post(String path, JSONObject body, ApiCallback<String> callback) {
        send(Request.Method.POST, path, body, callback);
    }

    public void put(String path, JSONObject body, ApiCallback<String> callback) {
        send(Request.Method.PUT, path, body, callback);
    }

    public void delete(String path, ApiCallback<String> callback) {
        send(Request.Method.DELETE, path, null, callback);
    }

    private void send(int method, String path, JSONObject body, ApiCallback<String> callback) {
        String url = BuildConfig.API_URL + path;

        StringRequest request = new StringRequest(
                method,
                url,
                callback::onSuccess,
                error -> handleError(error, callback)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                String token = session.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return body == null ? null : body.toString().getBytes(StandardCharsets.UTF_8);
            }

            // A API não informa o charset; sem isto os acentos ("Residência") viriam quebrados.
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String text = new String(response.data, StandardCharsets.UTF_8);
                return Response.success(text, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, 0, 1f));
        queue.add(request);
    }

    private void handleError(VolleyError error, ApiCallback<String> callback) {
        int status = error.networkResponse != null ? error.networkResponse.statusCode : -1;

        // Token vencido ou inválido: limpa a sessão e volta para o login.
        if (status == 401 && session.getToken() != null) {
            session.clear();
            Toast.makeText(appContext, "Sua sessão expirou. Entre novamente.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        }

        if (status == -1) {
            callback.onError("Não foi possível conectar à API. Verifique se ela está rodando.");
        } else {
            callback.onError("Algo deu errado (erro " + status + "). Tente novamente.");
        }
    }
}
