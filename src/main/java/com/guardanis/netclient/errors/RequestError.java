package com.guardanis.netclient.errors;

import java.util.ArrayList;
import java.util.List;

public class RequestError {

    protected List<String> errors = new ArrayList<String>();

    public RequestError(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors(){
        return errors;
    }

    public boolean hasErrors(){
        return 0 < errors.size();
    }

    @Override
    public String toString() {
        String errorMessage = "";

        for(String s : errors)
            errorMessage += s + "\n";

        if(errorMessage.length() > 3)
            errorMessage = errorMessage.substring(0, errorMessage.length() - 1);

        return errorMessage;
    }

}
