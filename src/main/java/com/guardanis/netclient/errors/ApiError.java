package com.guardanis.netclient.errors;

import com.guardanis.netclient.WebResult;

public class ApiError extends RequestError {

    private WebResult result;

    public ApiError(WebResult result, ErrorParser parser){
        super(parser.parseErrorMessages(result));
        this.result = result;
    }

    public WebResult getResult() {
        return result;
    }

}
