package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.errors.ErrorParser;
import com.guardanis.netclient.tools.NetUtils;

import java.io.UnsupportedEncodingException;
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
    public ApiRequest<T> setTargetUrl(String targetUrl){
        this.targetUrl = NetUtils.getInstance(context).getApiUrl() + targetUrl.trim();
        return this;
    }

    @Override
    protected void setRequestProperties(HttpURLConnection conn){
        super.setRequestProperties(conn);

        GlobalApiRequestProperties.getInstance(context)
                .addProperties(conn);
    }

    @Override
    protected URL buildUrl() throws MalformedURLException, UnsupportedEncodingException {
        return new URL(GlobalApiUrlParams.getInstance(context)
                .addAdditions(targetUrl));
    }

    @Override
    protected ErrorParser getErrorParser(){
        return errorParser == null
                ? NetUtils.getInstance(context)
                        .getApiErrorParser()
                : errorParser;
    }
}
