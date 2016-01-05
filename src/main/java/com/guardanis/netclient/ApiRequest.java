package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.errors.ApiError;
import com.guardanis.netclient.errors.DefaultErrorParser;
import com.guardanis.netclient.tools.NetUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ApiRequest<T> extends WebRequest<T> {

    public ApiRequest(Context context, ConnectionType connectionType) {
        super(context, connectionType);
    }

    public ApiRequest(Context context, ConnectionType connectionType, String targetUrl) {
        super(context, connectionType, targetUrl);
    }

    @Override
    protected void setRequestProperties(HttpURLConnection conn){
        super.setRequestProperties(conn);

        NetUtils.getInstance(context)
                .addBasicAuthRequestProperty(conn);

        NetUtils.getInstance(context)
                .addVersionRequestProperty(conn);

        conn.setRequestProperty("Accept-Encoding", NetUtils.getInstance(context).getAcceptEncodingProperty());
    }

    @Override
    protected URL buildUrl() throws MalformedURLException {
        return new URL(GlobalApiUrlParams.getInstance(context).addAdditions(targetUrl));
    }

    @Override
    protected void onResponseReceived(WebResult result) throws Exception {
        final ApiError errors = new ApiError(result, new DefaultErrorParser(context));
        if(errors.hasErrors()){
            postToOriginalThread(new Runnable() {
                public void run() {
                    if(failListener != null)
                        failListener.onFail(errors);
                }
            });
        }
        else super.onResponseReceived(result);
    }
}
