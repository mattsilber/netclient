package com.guardanis.netclient;

import android.content.Context;

import com.guardanis.netclient.tools.NetUtils;

import java.util.HashMap;
import java.util.Map;

public class GlobalApiUrlParams {

    private static GlobalApiUrlParams instance;
    public static GlobalApiUrlParams getInstance(Context context) {
        if(instance == null)
            instance = new GlobalApiUrlParams(context);

        return instance;
    }

    private static final String PREFS = "global_api_params_prefs";
    private static final String PREF_KEY_PRE = "addition_";

    private Context context;
    private Map<String, String> globalUrlAdditions = new HashMap<String, String>();

    protected GlobalApiUrlParams(Context context) {
        this.context = context.getApplicationContext();

        for(Map.Entry<String, ?> map : context.getSharedPreferences(PREFS, 0).getAll().entrySet()){
            try{
                if(map.getKey().startsWith(PREF_KEY_PRE) && !String.valueOf(map.getValue()).equals(""))
                    globalUrlAdditions.put(map.getKey().substring(PREF_KEY_PRE.length()), String.valueOf(map.getValue()));
            }
            catch(Exception e){ e.printStackTrace(); }
        }
    }

    public void register(String key, String value) {
        globalUrlAdditions.put(key, value);

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .putString(PREF_KEY_PRE + key, value)
                .commit();
    }

    public void unregister(String key) {
        globalUrlAdditions.remove(key);

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .remove(PREF_KEY_PRE + key)
                .commit();
    }

    public String addAdditions(String url) {
        if(!globalUrlAdditions.isEmpty()){
            url += url.contains("?") ? "&" : "?";

            for(String key : globalUrlAdditions.keySet())
                url += key + "=" + globalUrlAdditions.get(key) + "&";

            url = url.substring(0, url.length() - 1);
        }

        NetUtils.getInstance(context)
                .log("URL (with additions): " + url);

        return url;
    }
}
