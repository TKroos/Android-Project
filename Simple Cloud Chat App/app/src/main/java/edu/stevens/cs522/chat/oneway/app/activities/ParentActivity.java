package edu.stevens.cs522.chat.oneway.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

import edu.stevens.cs522.chat.oneway.app.services.RegisterService;
import edu.stevens.cs522.chat.oneway.client.R;

//import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

/**
 * Created by Xiang on 2015/2/2.
 */
public class ParentActivity  extends Activity implements Button.OnClickListener {

    private EditText serverUri;

    private EditText nameText;

    private Button okButton;

    public static String name;

    public static String uri;

    public static UUID uuid  =  null;

    public static int seqnum = 0;

    LocationManager locationManager = null;

    Location location = null;

    String provider;

    public static String latitude = "0";

    public static String longitude = "0";

    private static final String TAG = "BOOMBOOMTESTGPS";

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent);
        SharedPreferences mySharedPreferences= getSharedPreferences("registration ID", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        String registrationID = mySharedPreferences.getString("uuid", "");
        if (registrationID == "") {
            uuid = UUID.randomUUID();
            registrationID = uuid.toString();
            editor.putString("uuid", registrationID);
            editor.commit();
        }
        else uuid = UUID.fromString(registrationID);
        serverUri = (EditText) findViewById(R.id.enter_server_uri);
        nameText = (EditText) findViewById(R.id.enter_client_name);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(this);



        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        /*if(provider!=null && !provider.equals("")) {

            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 20000, 1, (android.location.LocationListener) this);
        }*/
        LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = mgr.getLastKnownLocation(provider);
        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
        }
    }

    /*public void onResume(){
        isGooglePlayServicesAvailable(this);
    }*/

    public void onClick(View view) {

        Intent intent = new Intent(this, DisplaySelection.class);
        Bundle bundle = new Bundle();
        name = nameText.getText().toString();
        bundle.putString(DisplaySelection.CLIENT_NAME_KEY, name);
        uri = serverUri.getText().toString();
        bundle.putString(DisplaySelection.SERVER_URI, uri);
        Intent registerIntent = new Intent(this, RegisterService.class);
        startService(registerIntent);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /*private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }*/
    /*protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }*/
/*    class LocationListener implements android.location.LocationListener {
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
    };*/
}
