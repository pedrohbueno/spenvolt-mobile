package com.spenvolt.mobile.api;

import android.content.Context;

import com.spenvolt.mobile.model.History;

import java.util.List;

public class HistoryApi {

    private final ApiClient api;

    public HistoryApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void findAll(ApiCallback<List<History>> callback) {
        api.get("/api/history", Json.list(History::fromJson, callback));
    }
}
