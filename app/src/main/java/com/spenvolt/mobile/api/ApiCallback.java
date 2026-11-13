package com.spenvolt.mobile.api;

public interface ApiCallback<T> {

    void onSuccess(T result);

    void onError(String message);
}
