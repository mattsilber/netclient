package com.guardanis.netclient.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.guardanis.netclient.R;
import com.guardanis.netclient.errors.DefaultErrorParser;
import com.guardanis.netclient.errors.ErrorParser;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
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

    private static final String PREFS = "nc__netutils_preferences";
    private static final String PREF__GENERAL_ERROR_PARSER_CLASS = "nc__general_error_parser";
    private static final String PREF__API_ERROR_PARSER_CLASS = "nc__api_error_parser";

    private Context context;
    private String apiUrl;

    private boolean loggingEnabled;

    private ErrorParser generalErrorParser;
    private ErrorParser apiErrorParser;

    protected NetUtils(Context context){
        this.context = context.getApplicationContext();
        this.apiUrl = context.getString(R.string.nc__api_url);

        this.loggingEnabled = context.getResources()
                .getBoolean(R.bool.nc__log_enabled);

        this.generalErrorParser = loadErrorParserClass(PREF__GENERAL_ERROR_PARSER_CLASS);
        this.apiErrorParser = loadErrorParserClass(PREF__API_ERROR_PARSER_CLASS);
    }

    private ErrorParser loadErrorParserClass(String key){
        try{
            return (ErrorParser) Class.forName(context.getSharedPreferences(PREFS, 0)
                            .getString(key, DefaultErrorParser.class.getName()))
                    .newInstance();
        }
        catch(Throwable e){
            log(e.getMessage());

            if(context.getResources().getBoolean(R.bool.nc__default_on_error_parser_load_failure))
                return new DefaultErrorParser();

            throw new RuntimeException(e);
        }
    }

    public String getApiUrl(){
        return apiUrl;
    }

    public NetUtils overrideApiUrl(String apiUrl){
        this.apiUrl = apiUrl;
        return this;
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
        if(isApiVersionHeaderEnabled())
            conn.setRequestProperty(context.getString(R.string.nc__api_version_header_name),
                    context.getString(R.string.nc__api_version_header_value));
    }

    public boolean isApiVersionHeaderEnabled(){
        return context.getResources()
                .getBoolean(R.bool.nc__api_version_header_enabled);
    }

    public String getContentTypeProperty(){
        return context.getString(R.string.nc__api_property_content_type);
    }

    public String getAcceptProperty(){
        return context.getString(R.string.nc__api_property_accept);
    }

    public String encodeParams(Map<String, String> params) throws UnsupportedEncodingException {
        if(params == null)
            return "";

        String encoded = "";

        for(String key : params.keySet())
            encoded += getUrlEncodedValue(key) + "=" + getUrlEncodedValue(params.get(key)) + "&";

        return encoded.substring(0, encoded.length() - 1);
    }

    public String getUrlEncodedValue(String toEncode) throws UnsupportedEncodingException {
        return URLEncoder.encode(toEncode,
                context.getString(R.string.nc__api_encoding));
    }

    public NetUtils setGeneralErrorParser(Class parserClass){
        try{
            ErrorParser parser = (ErrorParser) Class.forName(parserClass.getName())
                    .newInstance();

            this.generalErrorParser = parser;
        }
        catch(Throwable e){
            log(e.getMessage());

            throw new RuntimeException(e);
        }

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .putString(PREF__GENERAL_ERROR_PARSER_CLASS, parserClass.getName())
                .commit();

        return this;
    }

    public ErrorParser getGeneralErrorParser(){
        return generalErrorParser;
    }

    public NetUtils setApiErrorParser(Class parserClass){
        try{
            ErrorParser parser = (ErrorParser) Class.forName(parserClass.getName())
                    .newInstance();

            this.apiErrorParser = parser;
        }
        catch(Throwable e){
            log(e.getMessage());

            throw new RuntimeException(e);
        }

        context.getSharedPreferences(PREFS, 0)
                .edit()
                .putString(PREF__API_ERROR_PARSER_CLASS, parserClass.getName())
                .commit();

        return this;
    }

    public ErrorParser getApiErrorParser(){
        return apiErrorParser;
    }

    public static void close(Closeable closeable){
        try{
            if(closeable != null)
                closeable.close();
        }
        catch(Throwable e){ e.printStackTrace(); }
    }

    public void log(String message){
        if(loggingEnabled)
            Log.d(TAG, message);
    }

}
