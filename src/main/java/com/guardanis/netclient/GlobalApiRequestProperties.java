package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.tools.NetUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class GlobalApiRequestProperties {

    private static GlobalApiRequestProperties instance;
    public static GlobalApiRequestProperties getInstance(Context context) {
        if(instance == null)
            instance = new GlobalApiRequestProperties(context);

        return instance;
    }

    private static final String PREFS = "nc__global_api_request_properties";
    private static final String PREF_KEY_PRE = "header_";

    private Context context;
    private Map<String, String> requestProperties = new HashMap<String, String>();

    protected GlobalApiRequestProperties(Context context) {
        this.context = context.getApplicationContext();

        for(Map.Entry<String, ?> map : context.getSharedPreferences(PREFS, 0).getAll().entrySet()){
            try{
                if(map.getKey().startsWith(PREF_KEY_PRE) && !String.valueOf(map.getValue()).equals(""))
                    requestProperties.put(map.getKey().substring(PREF_KEY_PRE.length()),
                            String.valueOf(map.getValue()));
            }
            catch(Exception e){ e.printStackTrace(); }
        }
    }

    public GlobalApiRequestProperties register(String key, String value) {
        requestProperties.put(key, value);

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .putString(PREF_KEY_PRE + key, value)
                .commit();

        return this;
    }

    public GlobalApiRequestProperties unregister(String key) {
        requestProperties.remove(key);

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .remove(PREF_KEY_PRE + key)
                .commit();

        return this;
    }

    public GlobalApiRequestProperties addProperties(HttpURLConnection conn) {
        NetUtils.getInstance(context)
                .addBasicAuthRequestProperty(conn);

        NetUtils.getInstance(context)
                .addVersionRequestProperty(conn);

        for(String key : requestProperties.keySet())
            conn.setRequestProperty(key, requestProperties.get(key));

        return this;
    }
}
