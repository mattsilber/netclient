package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

public class GeneralError extends RequestError {

    private Throwable throwable;
    private boolean connectionIssue = false;

    public GeneralError(Context context, Throwable throwable){
        super(new ArrayList<String>());

        this.throwable = throwable;

        if(isLikelyConnectionError()){
            this.connectionIssue = true;
            errors.add(context.getResources().getString(R.string.nc__error_connection));
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    private boolean isLikelyConnectionError() {
        return throwable != null &&
                (throwable instanceof SocketTimeoutException
                || throwable instanceof SocketException
                || throwable instanceof InterruptedIOException
                || throwable instanceof UnknownHostException
                || throwable instanceof SSLException);
    }

    public boolean isConnectionIssue(){
        return connectionIssue;
    }
}
