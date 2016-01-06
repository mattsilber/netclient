package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.R;
import com.guardanis.netclient.WebResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultErrorParser implements ErrorParser {

    private Context context;

    public DefaultErrorParser(Context context){
        this.context = context;
    }

    @Override
    public List<String> parseErrorMessages(WebResult result) {
        List<String> errorMessages = new ArrayList<String>();

        if(!result.isSuccessful() || !result.isResponseCodeKnown()){
            JSONObject obj = null;
            try{
                obj = result.getResponseJson();
            }
            catch(Exception e){ e.printStackTrace(); }

            if(obj != null)
                return parseErrors(result, obj);
            else errorMessages.add(context.getString(R.string.nc__error_unknown));
        }

        return errorMessages;
    }

    private List<String> parseErrors(WebResult result, JSONObject obj) {
        List<String> errorMessages = new ArrayList<String>();

        JSONObject errors = obj.optJSONObject("errors");
        if(errors == null)
            errorMessages.add(obj.optString("error", context.getString(R.string.nc__error_unknown)));
        else return parseErrorsList(errors);

        return errorMessages;
    }

    protected List<String> parseErrorsList(JSONObject errors) {
        List<String> errorMessages = new ArrayList<String>();

        try{
            Iterator i = errors.keys();
            while(i.hasNext()){
                String title = String.valueOf(i.next());
                String messageTitle = getBaseErrorTitle(title);

                JSONArray messageArray = errors.optJSONArray(title);
                for(int j = 0; j < messageArray.length(); j++)
                    errorMessages.add((messageTitle.length() < 1 ? "" : (messageTitle + " ")) + messageArray.getString(j) + ".");
            }
        }
        catch(Exception e){ e.printStackTrace(); }

        return errorMessages;
    }

    private static String getBaseErrorTitle(String title) {
        if(title.equals("base"))
            return "";

        String correctTitle = title.replace("_", " ")
                .replace(".", " ");

        return Character.toUpperCase(title.charAt(0)) + correctTitle.substring(1);
    }
}
