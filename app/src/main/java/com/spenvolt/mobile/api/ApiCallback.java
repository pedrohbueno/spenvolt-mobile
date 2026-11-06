package com.spenvolt.mobile.api;

/**
 * Resposta de uma chamada à API.
 * onSuccess recebe o resultado já convertido; onError recebe uma mensagem pronta para mostrar.
 */
public interface ApiCallback<T> {

    void onSuccess(T result);

    void onError(String message);
}
