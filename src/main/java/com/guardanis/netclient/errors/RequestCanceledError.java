package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;

import java.util.ArrayList;

public class RequestCanceledError extends RequestError {

    public RequestCanceledError(Context context) {
        super(new ArrayList<String>());
        errors.add(context.getString(R.string.nc__error_canceled));
    }

}
