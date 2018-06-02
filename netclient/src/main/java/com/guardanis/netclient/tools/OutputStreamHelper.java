package com.guardanis.netclient.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class OutputStreamHelper {

    private OutputStream stream;
    private OutputStreamWriter writer;

    public OutputStreamHelper(OutputStream stream)  {
        this.stream = stream;
    }

    public void writeOrIgnore(String params) throws IOException {
        if (!isWritingAllowed(params))
            return;

        writer = new OutputStreamWriter(stream);
        writer.write(params);
        writer.flush();
    }

    public void closeConnection() {
        NetUtils.close(writer);

        this.stream = null;
    }

    public static OutputStreamHelper createWritableInstance(HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);

        return new OutputStreamHelper(connection.getOutputStream());
    }

    public static boolean isWritingAllowed(String params) {
        return params != null && 0 < params.length();
    }
}
