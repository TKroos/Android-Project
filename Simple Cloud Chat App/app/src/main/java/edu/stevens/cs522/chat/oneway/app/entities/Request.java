package edu.stevens.cs522.chat.oneway.app.entities;

import android.net.Uri;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Xiang on 2015/3/14.
 */
public abstract class Request implements Parcelable {
    public long clientID;
    public UUID registrationID; // sanity check

    // App-­‐specific HTTP request headers.
    public abstract Map<String, String> getHeaders();

    //Chat service URI with parameters e.g. query string parameters.
    public abstract Uri getRequestUri();

    //JSON body (if not null) for request data not passed in headers.
    /*public String getRequestEntity() throws IOException {
        String requestEntity = request.getRequestEntity();
        if (requestEntity != null){
            connection.setDoOutput(true);
            connection.setRequestProperty("CONTENT_TYPE", "application/json");
            byte[] outputEntity = requestEntity.getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(outputEntity.length);
            OutputStream out  = new BufferedOutputStream(connection.getOutputStream());
            out.write(outputEntity);
            out.flush();
            out.close();
        }
    }*/


    // Define your own Response class, including HTTP response code.
    /*public Response getResponse(HttpURLConnection connection, JsonReader rd*//*Null for streaming*//*) {
    }*/
}

/*
public abstract class Response implements Parcelable {
    public boolean isValid();
}*/
