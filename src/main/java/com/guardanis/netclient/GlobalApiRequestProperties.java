package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.tools.NetUtils;
import com.guardanis.netclient.tools.RequestProperties;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class GlobalApiRequestProperties extends RequestProperties {

    private static GlobalApiRequestProperties instance;
    public static GlobalApiRequestProperties getInstance(Context context) {
        if(instance == null)
            instance = new GlobalApiRequestProperties(context);

        return instance;
    }

    private static final String PREFS = "nc__global_api_request_properties";
    private static final String PREF_KEY_PRE = "header_";

    protected GlobalApiRequestProperties(Context context) {
        super(context, PREFS, PREF_KEY_PRE);
    }

    @Override
    public GlobalApiRequestProperties addProperties(HttpURLConnection conn) {
        NetUtils.getInstance(context)
                .addBasicAuthRequestProperty(conn);

        NetUtils.getInstance(context)
                .addVersionRequestProperty(conn);

        super.addProperties(conn);

        return this;
    }
}
