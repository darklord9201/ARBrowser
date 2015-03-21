package com.saugat.arbrowser;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity extends Activity implements ConnectionCallbacks,OnConnectionFailedListener, LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private boolean mRequestingLocationUpdates = true;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private TextView lblLocation;
    private Button btnShowLocation;
    private Button btnUpdateLocation;
    private Location cLocation;
    private Button btnCamera;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.saugat.arbrowser.R.layout.homescreen);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnUpdateLocation = (Button) findViewById(R.id.btnUpdateLocation);

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        //On Clicks

        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        btnUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
       protected void onStart() {
       super.onStart();
       if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayLocation(){
        cLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(cLocation != null){
            double latitude = cLocation.getLatitude();
            double longitude = cLocation.getLongitude();
            double altitude = cLocation.getAltitude();

            lblLocation.setText(latitude + "," + longitude + "," + altitude);
        }else {
            lblLocation.setText(
                    "Couldn't get the location. Make sure location is enabled on the device"
            );
        }
    }

    private void createLocationRequest(){
       mLocationRequest = new LocationRequest();
       mLocationRequest.setInterval(UPDATE_INTERVAL);
       mLocationRequest.setFastestInterval(FATEST_INTERVAL);
       mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
       mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public void onLocationChanged(Location location) {
        // Assign the new location
        cLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }




    @Override
    public void onConnected(Bundle arg0) {
        startLocationUpdates();
        displayLocation();
    }

    public void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,mLocationRequest,this
        );
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
