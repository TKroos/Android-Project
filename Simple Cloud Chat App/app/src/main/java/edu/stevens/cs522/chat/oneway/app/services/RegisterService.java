package edu.stevens.cs522.chat.oneway.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import edu.stevens.cs522.chat.oneway.app.activities.ParentActivity;
import edu.stevens.cs522.chat.oneway.app.entities.Request;

/**
 * Created by Xiang on 2015/3/14.
 */
public class RegisterService extends IntentService {
    HttpURLConnection httpConn;
    Map<String, List<String>> map;
    public static String clientID = "1";
    public RegisterService() {
        super("RequestService");
    }
    public void onCreate() {
        super.onCreate();
    }
    protected void onHandleIntent(Intent intent) {
        InputStream in = null;
        try {
            OpenHttpConnection(ParentActivity.uri);
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        catch (JSONException e2){
            Log.d("JSON", e2.getLocalizedMessage());
        }
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    public void setIntentRedelivery(boolean enabled) {
        super.setIntentRedelivery(enabled);
    }
    private void OpenHttpConnection(String urlString) throws IOException, JSONException {
        InputStream in = null;
        int response = -1;
        Request request = null;
        String query = String.format("username=%s&regid=%s",
                URLEncoder.encode(ParentActivity.name, "UTF-8"),
                URLEncoder.encode(ParentActivity.uuid.toString(), "UTF-8"));
        URL url = new URL(urlString + "?" + query);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            httpConn.setRequestProperty("CONNECTION", "Keep-Alive");
            httpConn.addRequestProperty("X-latitude", ParentActivity.latitude);
            httpConn.addRequestProperty("X-longitude", ParentActivity.longitude);
            httpConn.connect();
            throwErrors(httpConn);
            String[] temp2 = httpConn.getHeaderField("Content-Location").split("/");
            clientID = temp2[temp2.length-1];
            httpConn.disconnect();
        }
        catch (Exception ex){
            httpConn.disconnect();
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
    }

    void throwErrors (HttpURLConnection connection) throws IOException{
        final int status = connection.getResponseCode();
            if (status < 200 || status >= 300){
            String exceptionMessage = "Error response " + status + " " + connection.getResponseMessage() + " for " + connection.getURL();
            throw new IOException(exceptionMessage);
        }
    }
}
