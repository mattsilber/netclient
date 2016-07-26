package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;

public class RequestTimeoutError extends RequestError {

    public RequestTimeoutError(Context context) {
        super(context.getString(R.string.nc__error_timed_out));
    }

}
