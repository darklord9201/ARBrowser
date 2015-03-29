package com.saugat.arbrowser;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Asset;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.tools.io.AssetsManager;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class HomeActivity extends Activity implements ConnectionCallbacks,OnConnectionFailedListener, LocationListener {

    AssetsExtracter mTask;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1000;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.saugat.arbrowser.R.layout.homescreen);

        mTask = new AssetsExtracter();
        mTask.execute(0);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnUpdateLocation = (Button) findViewById(R.id.btnUpdateLocation);
        btnCamera = (Button) findViewById(R.id.btnCamera);

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
                Intent intent = new Intent(getApplicationContext(), modelTest.class);
                startActivity(intent);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(getBaseContext() , CameraActivity.class);
               startActivity(i);
            }
        });
    }

    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {
        @Override
        protected Boolean doInBackground(Integer... params)
        {
            try
            {
                // Extract all assets except Menu. Overwrite existing files for debug build only.
                final String[] ignoreList = {"Menu", "webkit", "sounds", "images", "webkitsec"};
                AssetsManager.extractAllAssets(getApplicationContext(), "", ignoreList, BuildConfig.DEBUG);
            }
            catch (IOException e)
            {
                MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
            }

            return true;
        }
    }


    @Override
       protected void onStart() {
       super.onStart();
       if (mGoogleApiClient != null) {
           Toast.makeText(this, "NO GPS",
                   Toast.LENGTH_LONG).show();
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
        if(mRequestingLocationUpdates){
            startLocationUpdates();
            displayLocation();
        }
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
