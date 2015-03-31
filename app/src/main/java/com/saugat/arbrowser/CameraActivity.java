package com.saugat.arbrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.locks.Lock;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;


import java.security.Provider;


public class CameraActivity extends ARViewActivity implements SensorsComponentAndroid.Callback{

    /*
        Geometry
    * */
    private IGeometry mIslingtonCollege;
    private IGeometry mHome;

    private IRadar mRadar;

    private TextView coOrdinates;
    private Button btnShowLocation;

    @Override
    protected int getGUILayout() {

        return R.layout.ar_test;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {
        boolean result = metaioSDK.setTrackingConfiguration("GPS");

        metaioSDK.setLLAObjectRenderingLimits(5, 200);
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

//        LLACoordinate islingtonCollege = new LLACoordinate(27.7079649, 85.326495, currentPosition.getAltitude() , currentPosition.getAccuracy());
//        LLACoordinate home = new LLACoordinate(27.7366866, 85.357272, currentPosition.getAltitude() , currentPosition.getAccuracy());
//        mIslingtonCollege = createPOIGeometry(islingtonCollege);
//        mHome = createPOIGeometry(home);
//
//        String metaioManModel = AssetsManager.getAssetPath("metaioman.md2");

//        if(metaioManModel != null){
//            mIslingtonCollege = metaioSDK.createGeometry(metaioManModel);
//            mHome = metaioSDK.createGeometry(metaioManModel);
//            if(mIslingtonCollege != null){
//                mIslingtonCollege.setTranslationLLA(islingtonCollege);
//                mIslingtonCollege.setScale(50);
//            }
//            else{
//                MetaioDebug.log(Log.ERROR, "Error loading geometry" + metaioManModel);
//            }
//        }
        loadGPScontent();

    }



    private IGeometry createPOIGeometry(LLACoordinate lla){

        String path = AssetsManager.getAssetPath("ExamplePOI.obj");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(new Vector3d(4.0f, 4.0f, 4.0f));
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
            return null;
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_test);

        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);

        coOrdinates = (TextView)findViewById(R.id.coordinate);
        btnShowLocation = (Button)findViewById(R.id.showLocation);


        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation();
            }
        });
    }

    public void showLocation(){


    }

    @Override
    public void onDrawFrame(){
        if(metaioSDK != null && mSensors != null){
            SensorValues sensorValues = mSensors.getSensorValues();

            float heading = 0.0f;
            if(sensorValues.hasAttitude()){
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v.normalize();

                heading = (float)(-Math.atan2(v.getY(), v.getX() - Math.PI / 2.0));
            }
            IGeometry geos[] = new IGeometry[]{mIslingtonCollege};
            Rotation rot =new Rotation((float) (Math.PI / 2.0), 0.0f, -heading);

            for(IGeometry geo: geos){
                geo.setRotation(rot);
            }
        }
        super.onDrawFrame();
    }

    @Override
    public void onGravitySensorChanged(float[] floats) {

    }

    @Override
    public void onHeadingSensorChanged(float[] floats) {

    }

    @Override
    public void onLocationSensorChanged(LLACoordinate llaCoordinate) {
        updateGeometries(mSensors.getLocation());
    }

    public void loadGPScontent(){
        try{
                mIslingtonCollege = metaioSDK.createGeometryFromImage(createBillboardTexture("ABCD"),true);
                updateGeometries(mSensors.getLocation());

            mRadar = metaioSDK.createRadar();
            mRadar.setBackgroundTexture(AssetsManager.getAssetPath("radar.png"));
            mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("yellow.png"));
            mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

            mRadar.add(mIslingtonCollege);

        }catch(Exception e){

        }
    }
    private void updateGeometries(LLACoordinate location){
        LLACoordinate currentPosition = mSensors.getLocation();
        LLACoordinate college = new LLACoordinate(27.7366866, 85.357272, currentPosition.getAltitude() , currentPosition.getAccuracy());

        if(mIslingtonCollege != null){
            mIslingtonCollege.setTranslationLLA(college);
        }

    }

    private String createBillboardTexture(String billBoardTitle)
    {
        try
        {
            final String texturepath = getCacheDir() + "/" + billBoardTitle + ".png";
            Paint mPaint = new Paint();

            // Load background image (256x128), and make a mutable copy
            Bitmap billboard = null;

            //reading billboard background
            String filepath = AssetsManager.getAssetPath("poi.png");
            Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);

            billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);

            Canvas c = new Canvas(billboard);

            mPaint.setColor(Color.WHITE);
            mPaint.setTextSize(24);
            mPaint.setTypeface(Typeface.DEFAULT);

            float y = 40;
            float x = 30;

            // Draw POI name
            if (billBoardTitle.length() > 0)
            {
                String n = billBoardTitle.trim();

                final int maxWidth = 160;

                int i = mPaint.breakText(n, true, maxWidth, null);
                c.drawText(n.substring(0, i), x, y, mPaint);

                // Draw second line if valid
                if (i < n.length())
                {
                    n = n.substring(i);
                    y += 20;
                    i = mPaint.breakText(n, true, maxWidth, null);

                    if (i < n.length())
                    {
                        i = mPaint.breakText(n, true, maxWidth - 20, null);
                        c.drawText(n.substring(0, i) + "...", x, y, mPaint);
                    } else
                    {
                        c.drawText(n.substring(0, i), x, y, mPaint);
                    }
                }

            }

            // writing file
            try
            {
                FileOutputStream out = new FileOutputStream(texturepath);
                billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
                MetaioDebug.log("Texture file is saved to "+texturepath);
                return texturepath;
            } catch (Exception e) {
                MetaioDebug.log("Failed to save texture file");
                e.printStackTrace();
            }

            billboard.recycle();
            billboard = null;

        } catch (Exception e)
        {
            MetaioDebug.log("Error creating billboard texture: " + e.getMessage());
            MetaioDebug.printStackTrace(Log.DEBUG, e);
            return null;
        }
        return null;
    }

}
