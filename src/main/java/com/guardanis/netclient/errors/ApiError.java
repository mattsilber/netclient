package com.guardanis.netclient.errors;

import com.guardanis.netclient.WebResult;

import java.util.List;

public class ApiError extends RequestError {

    private WebResult result;

    public ApiError(WebResult result, List<String> errors){
        super(errors);
        this.result = result;
    }

    public WebResult getResult() {
        return result;
    }

}
