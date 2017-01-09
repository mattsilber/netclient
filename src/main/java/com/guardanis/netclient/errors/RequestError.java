package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

public class RequestError {

    protected List<String> errors = new ArrayList<String>();

    private Throwable throwable;
    private boolean connectionIssue = false;

    public RequestError(Context context, Throwable throwable){
        this.throwable = throwable;

        if(isLikelyConnectionError(throwable)){
            this.connectionIssue = true;

            errors.add(context.getResources()
                    .getString(R.string.nc__error_connection));
        }
        else
            errors.add(context.getResources()
                    .getString(R.string.nc__error_unknown));
    }

    public RequestError(String error) {
        this.errors.add(error);
    }

    public RequestError(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors(){
        return errors;
    }

    public boolean hasErrors(){
        return 0 < errors.size();
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    public String toString(String delimiter){
        if(errors == null || errors.size() < 1)
            return "Something went wrong.";

        String errorMessage = errors.get(0);

        for(int i = 1; i < errors.size(); i++)
            errorMessage += delimiter + errors.get(i);

        return errorMessage;
    }

    protected boolean isLikelyConnectionError(Throwable throwable) {
        return throwable != null &&
                (throwable instanceof SocketTimeoutException
                        || throwable instanceof SocketException
                        || throwable instanceof InterruptedIOException
                        || throwable instanceof UnknownHostException
                        || throwable instanceof SSLException);
    }

    public boolean isKnownConnectionIssue(){
        return connectionIssue;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
