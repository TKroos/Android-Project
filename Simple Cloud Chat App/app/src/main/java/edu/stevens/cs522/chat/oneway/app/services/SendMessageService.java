package edu.stevens.cs522.chat.oneway.app.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.stevens.cs522.chat.oneway.app.activities.ParentActivity;
import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;

/**
 * Created by Xiang on 2015/3/20.
 */
public class SendMessageService extends IntentService {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    HttpURLConnection httpConn;
    String date;
    String message;
    String chatroom;
    ContentValues contentValues = new ContentValues();
    private ArrayList<Integer> seqnums = new ArrayList<Integer>();
    ArrayList<String> clients = new ArrayList<String>();
    ArrayList<String> texts = new ArrayList<String>();
    ArrayList<String> senders = new ArrayList<String>();
    ArrayList<String> chatrooms = new ArrayList<String>();
    ArrayList<String> latitudes = new ArrayList<String>();
    ArrayList<String> longitudes = new ArrayList<String>();
    JsonReader rd;
    private ContentResolver contentResolver;
    public SendMessageService() {
        super("SendMessageService");
    }
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        if ((mLocationListeners[0].mLastLocation.getLatitude() != 0)||(mLocationListeners[0].mLastLocation.getLongitude() != 0)){
            ParentActivity.latitude = String.valueOf(mLocationListeners[0].mLastLocation.getLatitude());
            ParentActivity.longitude = String.valueOf(mLocationListeners[0].mLastLocation.getLongitude());
        }
        else if ((mLocationListeners[1].mLastLocation.getLatitude() != 0)||(mLocationListeners[1].mLastLocation.getLongitude() != 0)){
            ParentActivity.latitude = String.valueOf(mLocationListeners[1].mLastLocation.getLatitude());
            ParentActivity.longitude = String.valueOf(mLocationListeners[1].mLastLocation.getLongitude());
        }
    }
    protected void onHandleIntent(Intent intent) {
        InputStream in = null;
        contentResolver = getContentResolver();
        try {
            message = intent.getStringExtra("message");
            chatroom = intent.getStringExtra("chatroom");
            OpenHttpConnection(ParentActivity.uri);
            for (int i = 0; i < texts.size(); i++) {
                writeToProvider(contentValues, senders.get(i), texts.get(i), seqnums.get(i), chatrooms.get(i), latitudes.get(i), longitudes.get(i));
                contentResolver.insert(ChatProvider.MESSAGE_TABLE_URI, contentValues);
            }
            ParentActivity.seqnum = seqnums.get( seqnums.size() - 1 );
            seqnums.clear();
            senders.clear();
            texts.clear();
            chatrooms.clear();
            latitudes.clear();
            longitudes.clear();
            Intent i = new Intent("Chat App");
            i.putExtra("receive_or_send", "receive");
            sendBroadcast(i);
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
        String query = String.format("regid=%s&seqnum=%s",
                URLEncoder.encode(ParentActivity.uuid.toString(), "UTF-8"),
                URLEncoder.encode(String.valueOf(ParentActivity.seqnum), "UTF-8"));
        URL url = new URL(urlString + "/" + RegisterService.clientID +"?" + query);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.addRequestProperty("X-latitude", ParentActivity.latitude);
            httpConn.addRequestProperty("X-longitude",ParentActivity.longitude);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            outputRequestEntity();
            httpConn.connect();
            throwErrors(httpConn);
            date = httpConn.getHeaderField("Date");
            rd = new JsonReader(new BufferedReader(new InputStreamReader(httpConn.getInputStream())));
            rd.beginObject();
            while (rd.peek() != JsonToken.END_OBJECT) {
                String name1 = rd.nextName();
                if (name1.equals("clients")) {
                    rd.beginArray();
                    while (rd.peek() != JsonToken.END_ARRAY) {
                        rd.beginObject();
                        while (rd.peek() != JsonToken.END_OBJECT) {
                            String name2 = rd.nextName();
                            if (name2.equals("sender")) {
                                rd.skipValue();
                            }
                            else rd.skipValue();
                        }
                        rd.endObject();
                    }
                    rd.endArray();
                } else {
                    rd.beginArray();
                    while (rd.peek() != JsonToken.END_ARRAY) {
                        rd.beginObject();
                        while (rd.peek() != JsonToken.END_OBJECT) {
                            String name = rd.nextName();
                            if (name.equals("seqnum")) {
                                seqnums.add(rd.nextInt());
                            }
                            else if (name.equals("sender")) {
                                senders.add(rd.nextString());
                            }
                            else if (name.equals("text")) {
                                texts.add(rd.nextString());
                            }
                            else if (name.equals("chatroom")) {
                                chatrooms.add(rd.nextString());
                            }
                            else if (name.equals("X-latitude")) {
                                latitudes.add(String.valueOf(rd.nextDouble()));
                            }
                            else if (name.equals("X-longitude")) {
                                longitudes.add(String.valueOf(rd.nextDouble()));
                            }
                            else rd.skipValue();
                        }
                        rd.endObject();
                    }
                    rd.endArray();
                }
            }
            rd.endObject();
            rd.close();
            httpConn.disconnect();
        }
        catch (Exception ex){
            httpConn.disconnect();
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
    }
    void outputRequestEntity() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("chatroom", chatroom);
        jsonObject.put("timestamp", 12345678);
        jsonObject.put("X-latitude", Double.valueOf(ParentActivity.latitude));
        jsonObject.put("X-longitude", Double.valueOf(ParentActivity.longitude));
        jsonObject.put("text", message);
        jsonArray.put(0, jsonObject);
        httpConn.setDoOutput(true);
        httpConn.setRequestProperty("Content-Type", "application/json");
        //httpConn.setRequestProperty("Accept", "application/json");
        byte[] outputEntity = jsonArray.toString(0).getBytes("UTF-8");
        //httpConn.setFixedLengthStreamingMode(outputEntity.length);
        //httpConn.setChunkedStreamingMode(0);
        /*OutputStream out = new BufferedOutputStream(httpConn.getOutputStream());
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));*/
        OutputStream os = httpConn.getOutputStream();
        //JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(os,"UTF-8")));
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.setIndent("  ");
        writer.beginArray();
        writer.beginObject();
        writer.name("chatroom").value(chatroom);
        writer.name("timestamp").value(12345678);
        writer.name("X-latitude").value(Double.valueOf(ParentActivity.latitude));
        writer.name("X-longitude").value(Double.valueOf(ParentActivity.longitude));
        writer.name("text").value(message);
        writer.endObject();
        writer.endArray();
        writer.flush();
        writer.close();
        /*out.write(outputEntity);
        out.flush();
        out.close();*/
    }
    void writeToProvider(ContentValues contentValues, String sender, String message, int message_id, String chatroom, String latitude, String longitude){
        contentValues.put(ChatProvider._ID, (byte[])null);
        contentValues.put(ChatProvider.MESSAGE, message);
        contentValues.put(ChatProvider.MESSAGE_ID, message_id);
        contentValues.put(ChatProvider.SENDER, sender);
        contentValues.put(ChatProvider.SENDER_ID, Integer.parseInt(RegisterService.clientID));
        contentValues.put(ChatProvider.DATE, date);
        contentValues.put(ChatProvider.CHATROOM, chatroom);
        contentValues.put(ChatProvider.LATITUDE, latitude);
        contentValues.put(ChatProvider.LONGITUDE, longitude);
        try {
            //Geocoder gcoder = new Geocoder(this);
            List<Address> addresses = new ArrayList<Address>();
            addresses = getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
            String[] ad = addresses.get(0).toString().split("\"");
            contentValues.put(ChatProvider.ADDRESS, ad[1]);
        }catch (Exception e){
            Log.e("Address", "No this address!");
            contentValues.put(ChatProvider.ADDRESS, "No address!");
        }

    }
    void throwErrors (HttpURLConnection connection) throws IOException{
        final int status = connection.getResponseCode();
        if (status < 200 || status >= 300){
            String exceptionMessage = "Error response " + status + " " + connection.getResponseMessage() + " for " + connection.getURL();
            throw new IOException(exceptionMessage);
        }
    }
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public static List<Address> getFromLocation(double lat, double lng, int maxResult){

        String address = String.format(Locale.US,"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="+Locale.getDefault().getCountry(), lat, lng);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        List<Address> retList = null;

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(stringBuilder.toString());


            retList = new ArrayList<Address>();


            if("OK".equalsIgnoreCase(jsonObject.getString("status"))){
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i=0;i<results.length();i++ ) {
                    JSONObject result = results.getJSONObject(i);
                    String indiStr = result.getString("formatted_address");
                    Address addr = new Address(Locale.US);
                    addr.setAddressLine(0, indiStr);
                    retList.add(addr);
                }
            }


        } catch (ClientProtocolException e) {
            Log.e(SendMessageService.class.getName(), "Error calling Google geocode webservice.", e);
        } catch (IOException e) {
            Log.e(SendMessageService.class.getName(), "Error calling Google geocode webservice.", e);
        } catch (JSONException e) {
            Log.e(SendMessageService.class.getName(), "Error parsing Google geocode webservice response.", e);
        }

        return retList;
    }

}