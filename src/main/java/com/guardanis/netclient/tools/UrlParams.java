package com.guardanis.netclient.tools;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class UrlParams {

    protected Context context;

    protected String preferenceTable;
    protected String preferencePrefix;

    protected Map<String, String> urlAdditions = new HashMap<String, String>();

    public UrlParams(Context context, String preferenceTable, String preferencePrefix) {
        this.context = context.getApplicationContext();
        this.preferenceTable = preferenceTable;
        this.preferencePrefix = preferencePrefix;

        for(Map.Entry<String, ?> map : context.getSharedPreferences(preferenceTable, 0).getAll().entrySet()){
            try{
                if(map.getKey().startsWith(preferencePrefix) && !String.valueOf(map.getValue()).equals(""))
                    urlAdditions.put(map.getKey().substring(preferencePrefix.length()),
                            String.valueOf(map.getValue()));
            }
            catch(Exception e){ e.printStackTrace(); }
        }
    }

    public UrlParams register(String key, String value) {
        urlAdditions.put(key, value);

        context.getSharedPreferences(preferenceTable, 0)
                .edit()
                .putString(preferencePrefix + key, value)
                .commit();

        return this;
    }

    public UrlParams unregister(String key) {
        urlAdditions.remove(key);

        context.getSharedPreferences(preferenceTable, 0)
                .edit()
                .remove(preferencePrefix + key)
                .commit();

        return this;
    }

    public String addAdditions(String url) throws UnsupportedEncodingException {
        if(!urlAdditions.isEmpty()){
            url += url.contains("?")
                    ? "&"
                    : "?";

            url += NetUtils.getInstance(context)
                    .encodeParams(urlAdditions);
        }

        return url;
    }
}
