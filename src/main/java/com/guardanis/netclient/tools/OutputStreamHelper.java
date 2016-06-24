package com.guardanis.netclient.tools;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class OutputStreamHelper {

    private HttpURLConnection connection;
    private OutputStreamWriter writer;

    public OutputStreamHelper(HttpURLConnection connection)  {
        this.connection = connection;
    }

    public void write(String params) throws IOException {
        if(!(params == null || params.length() < 1)){
            connection.setDoOutput(true);

            writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(params);
            writer.flush();
        }
    }

    public void closeConnection() {
        try{
            if(writer != null)
                writer.close();
        }
        catch(Exception e){ }

        connection = null;
    }

}
