package com.guardanis.netclient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebResult {

    private int responseCode;
    private String unparsedResponse;

    private long createdAtMs = 0;

    private Map<String, Object> extras = new HashMap<String, Object>();

    public WebResult(int responseCode, String unparsedResponse) {
        this(responseCode, unparsedResponse, System.currentTimeMillis());
    }

    public WebResult(int responseCode, String unparsedResponse, long createdAtMs) {
        this.responseCode = responseCode;
        this.unparsedResponse = unparsedResponse;
        this.createdAtMs = createdAtMs;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public WebResult setExtras(Map<String, Object> extras) {
        this.extras = extras;
        return this;
    }

    public WebResult putExtra(String key, Object data){
        this.extras.put(key, data);
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getUnparsedResponse() {
        return unparsedResponse;
    }

    public boolean isSuccessful() {
        return responseCode >= 200 && responseCode < 400;
    }

    public boolean isResponseCodeKnown() {
        return responseCode > 0;
    }

    public boolean isResponseJson() {
        try{
            new JSONObject(unparsedResponse);

            return true;
        }
        catch(JSONException e){ }

        return false;
    }

    public JSONObject getResponseJson() {
        try{
            return new JSONObject(unparsedResponse);
        }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }
}
