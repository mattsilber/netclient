package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.WebResult;

import java.util.List;

public interface ErrorParser {

    /**
     * @return A RequestError object or null if no errors are present. Non-null responses with
     * empty error messages are treated as if a null RequestError was returned.
     */
    public RequestError parseErrorMessages(Context context, WebResult result);
}
