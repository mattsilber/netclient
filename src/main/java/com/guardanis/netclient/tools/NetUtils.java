package com.guardanis.netclient.tools;

import android.content.Context;
import android.util.Log;

import com.guardanis.netclient.R;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import it.sauronsoftware.base64.Base64;

public class NetUtils {

    private static NetUtils instance;
    public static NetUtils getInstance(Context context) {
        if(instance == null)
            instance = new NetUtils(context);

        return instance;
    }

    private static final String TAG = "netclient";

    private Context context;
    private String apiUrl;

    protected NetUtils(Context context){
        this.context = context.getApplicationContext();
        this.apiUrl = context.getString(R.string.nc__api_url);
    }

    public String getApiUrl(){
        return apiUrl;
    }

    public void overrideApiUrl(String apiUrl){
        this.apiUrl = apiUrl;
    }

    public void addBasicAuthRequestProperty(HttpURLConnection conn){
        if(isBasicAuthEnabled())
            conn.setRequestProperty("Authorization", "Basic " + getBasicAuthEncodedHeader());
    }

    private String getBasicAuthEncodedHeader(){
        String value = context.getString(R.string.nc__api_basic_auth_username)
                + ":" + context.getString(R.string.nc__api_basic_auth_password);

        return new String(Base64.encode(value).getBytes());
    }

    public boolean isBasicAuthEnabled(){
        return context.getResources().getBoolean(R.bool.nc__api_basic_auth_enabled);
    }

    public void addVersionRequestProperty(HttpURLConnection conn){
        if(isBasicAuthEnabled())
            conn.setRequestProperty(context.getString(R.string.nc__api_version_header_name),
                    context.getString(R.string.nc__api_version_header_value));
    }

    public boolean isApiVersionHeaderEnabled(){
        return context.getResources().getBoolean(R.bool.nc__api_version_header_enabled);
    }

    public String getContentTypeProperty(){
        return context.getString(R.string.nc__api_property_content_type);
    }

    public String getAcceptProperty(){
        return context.getString(R.string.nc__api_property_accept);
    }

    public String getAcceptEncodingProperty(){
        return context.getString(R.string.nc__api_property_accept_encoding);
    }

    public String encodeParams(Map<String, String> params) throws Exception {
        if(params == null)
            return "";

        String encoded = "";
        for(String key : params.keySet())
            encoded += getUrlEncodedValue(key) + "=" + getUrlEncodedValue(params.get(key)) + "&";

        return encoded.substring(0, encoded.length() - 1);
    }

    public String getUrlEncodedValue(String toEncode) throws Exception {
        return URLEncoder.encode(toEncode, context.getString(R.string.nc__api_encoding));
    }

    public void log(String message){
        if(context.getResources().getBoolean(R.bool.nc__log_enabled))
            Log.d(TAG, message);
    }

}
