package com.guardanis.netclient.batch;

import com.guardanis.netclient.WebResult;
import com.guardanis.netclient.errors.RequestError;

public class BatchItemResponse<T> {

    private Batchable<T> batchable;

    private WebResult response;
    private T result;

    private RequestError error;

    public BatchItemResponse(Batchable<T> batchable){
        this.batchable = batchable;
    }

    public BatchItemResponse<T> setResponse(WebResult response) {
        this.response = response;

        return this;
    }

    public BatchItemResponse<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public BatchItemResponse<T> setError(RequestError error) {
        this.error = error;
        return this;
    }

    public Batchable<T> getBatchable() {
        return batchable;
    }

    public WebResult getResponse() {
        return response;
    }

    @Deprecated
    public T getData() {
        return getResult();
    }

    public T getResult() {
        return result;
    }

    public RequestError getError() {
        return error;
    }

    public boolean hasErrors(){
        return error != null && 0 < error.getErrors().size();
    }
}
