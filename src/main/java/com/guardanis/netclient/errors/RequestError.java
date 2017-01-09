package com.guardanis.netclient.errors;

import java.util.ArrayList;
import java.util.List;

public class RequestError {

    protected List<String> errors = new ArrayList<String>();

    public RequestError(String error) {
        this.errors.add(error);
    }

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
        return toString("\n");
    }

    public String toString(String delimiter){
        if(errors == null || errors.size() < 1)
            return "Something went wrong.";

        String errorMessage = errors.get(0);

        for(int i = 1; i < errors.size(); i++)
            errorMessage += delimiter + errors.get(i);

        return errorMessage;
    }

}
