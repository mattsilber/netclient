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

        try{
            JSONObject potentialErrors = result.getResponseJson();

            if(potentialErrors != null)
                errorMessages = parseErrors(potentialErrors);
        }
        catch(Exception e){ e.printStackTrace(); }

        if(!result.isSuccessful() && errorMessages.size() < 1)
            errorMessages.add(context.getString(R.string.nc__error_unknown));

        return errorMessages;
    }

    private List<String> parseErrors(JSONObject obj) {
        List<String> errorMessages = new ArrayList<String>();

        if(obj.has("errors"))
            return parseErrorsList(obj.optJSONObject("errors"));
        else if(obj.has("error"))
            errorMessages.add(obj.optString("error", context.getString(R.string.nc__error_unknown)));

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
                for(int j = 0; j < messageArray.length(); j++){
                    String message = (messageTitle.length() < 1 ? "" : (messageTitle + " ")) + messageArray.getString(j);

                    if(Character.isLetter(message.charAt(message.length() - 1)))
                        message += ".";

                    errorMessages.add(message);
                }
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
