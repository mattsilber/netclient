package com.guardanis.netclient.batch;

import com.guardanis.netclient.WebResult;
import com.guardanis.netclient.errors.RequestError;

public class BatchItemResponse<T> {

    private Batchable<T> batchable;

    private WebResult result;
    private T data;

    private RequestError error;

    public BatchItemResponse(Batchable<T> batchable){
        this.batchable = batchable;
    }

    public BatchItemResponse<T> setResult(WebResult result, T data) {
        this.result = result;
        this.data = data;
        return this;
    }

    public BatchItemResponse<T> setError(RequestError error) {
        this.error = error;
        return this;
    }

    public Batchable<T> getBatchable() {
        return batchable;
    }

    public WebResult getResult() {
        return result;
    }

    public T getData() {
        return data;
    }

    public RequestError getError() {
        return error;
    }

    public boolean hasErrors(){
        return error != null && 0 < error.getErrors().size();
    }
}
