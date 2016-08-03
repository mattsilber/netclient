package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.WebResult;

import java.util.List;

public interface ErrorParser {

    public List<String> parseErrorMessages(Context context, WebResult result);

}
