package com.guardanis.netclient;

import org.json.JSONException;
import org.json.JSONObject;

public class WebResult {

    private int responseCode;
    private String unparsedResponse;

    public WebResult(int responseCode, String unparsedResponse) {
        this.responseCode = responseCode;
        this.unparsedResponse = unparsedResponse;
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

}
