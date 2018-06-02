package com.guardanis.netclient.tools;

import android.content.Context;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class RequestProperties {

    protected Context context;

    protected String preferenceTable;
    protected String preferencePrefix;

    protected Map<String, String> requestProperties = new HashMap<String, String>();

    public RequestProperties(Context context, String preferenceTable, String preferencePrefix) {
        this.context = context.getApplicationContext();
        this.preferenceTable = preferenceTable;
        this.preferencePrefix = preferencePrefix;

        for(Map.Entry<String, ?> map : context.getSharedPreferences(preferenceTable, 0).getAll().entrySet()){
            try{
                if(map.getKey().startsWith(preferencePrefix) && !String.valueOf(map.getValue()).equals(""))
                    requestProperties.put(map.getKey().substring(preferencePrefix.length()),
                            String.valueOf(map.getValue()));
            }
            catch(Exception e){ e.printStackTrace(); }
        }
    }

    public RequestProperties register(String key, String value) {
        requestProperties.put(key, value);

        context.getSharedPreferences(preferenceTable, 0)
                .edit()
                .putString(preferencePrefix + key, value)
                .commit();

        return this;
    }

    public RequestProperties unregister(String key) {
        requestProperties.remove(key);

        context.getSharedPreferences(preferenceTable, 0)
                .edit()
                .remove(preferencePrefix + key)
                .commit();

        return this;
    }

    public RequestProperties addProperties(HttpURLConnection conn) {
        for(String key : requestProperties.keySet())
            conn.setRequestProperty(key, requestProperties.get(key));

        return this;
    }
}
