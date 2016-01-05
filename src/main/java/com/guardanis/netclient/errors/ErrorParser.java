package com.guardanis.netclient.errors;

import com.guardanis.netclient.WebResult;

import java.util.List;

public interface ErrorParser {

    public List<String> parseErrorMessages(WebResult result);

}
