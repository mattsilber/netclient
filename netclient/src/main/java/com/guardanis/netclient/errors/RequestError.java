package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;
import com.guardanis.netclient.WebResult;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

public class RequestError {

    protected List<String> errors = new ArrayList<String>();

    protected WebResult response;

    protected Map<String, Object> extras = new HashMap<String, Object>();
    protected boolean connectionIssue = false;

    public RequestError(Context context, Throwable throwable) {
        this(context, null, throwable);
    }

    public RequestError(Context context, WebResult response, Throwable throwable) {
        this.response = response;

        if(throwable == null)
            errors.add(context.getResources()
                    .getString(R.string.nc__error_unknown));
        else if(isLikelyConnectionError(throwable)){
            this.connectionIssue = true;

            errors.add(context.getResources()
                    .getString(R.string.nc__error_connection));
        }
        else
            errors.add(throwable.getMessage());
    }

    public RequestError(String error) {
        this.errors.add(error);
    }

    public RequestError(List<String> errors) {
        this.errors = errors;
    }

    public RequestError(WebResult response, List<String> errors) {
        this.response = response;
        this.errors = errors;
    }

    public List<String> getErrors(){
        return errors;
    }

    public boolean hasErrors(){
        return 0 < errors.size();
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public RequestError setExtras(Map<String, Object> extras) {
        this.extras = extras;
        return this;
    }

    public RequestError putExtra(String key, Object data){
        this.extras.put(key, data);
        return this;
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

    public boolean isKnownConnectionIssue() {
        return connectionIssue;
    }

    /**
     * @return the WebResult response, or null if there is none
     */
    public WebResult getResponse() {
        return response;
    }
}
