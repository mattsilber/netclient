package com.guardanis.netclient.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

public class InputStreamHelper {

    private InputStream inputStream;

    public InputStreamHelper(HttpURLConnection conn) throws NullPointerException, IOException {
        inputStream = conn.getResponseCode() < 400
                ? conn.getInputStream()
                : conn.getErrorStream();

        String contentEncoding = conn.getContentEncoding();

        if(contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip"))
            inputStream = new GZIPInputStream(inputStream);
    }

    public String read() throws IOException {
        String response = "";
        String line;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while((line = reader.readLine()) != null)
            response += line;

        reader.close();

        return response;
    }

    public void closeConnection() {
        NetUtils.close(inputStream);
    }

}
