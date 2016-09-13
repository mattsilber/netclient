package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.tools.NetUtils;
import com.guardanis.netclient.tools.UrlParams;

import java.io.UnsupportedEncodingException;

public class GlobalApiUrlParams extends UrlParams {

    private static GlobalApiUrlParams instance;
    public static GlobalApiUrlParams getInstance(Context context) {
        if(instance == null)
            instance = new GlobalApiUrlParams(context);

        return instance;
    }

    private static final String PREFS = "nc__global_api_url_params";
    private static final String PREF_KEY_PRE = "addition_";

    protected GlobalApiUrlParams(Context context) {
        super(context, PREFS, PREF_KEY_PRE);
    }

    @Override
    public String addAdditions(String url) throws UnsupportedEncodingException {
        url = super.addAdditions(url);

        NetUtils.getInstance(context)
                .log("URL (with additions): " + url);

        return url;
    }
}
