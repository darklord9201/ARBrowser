package com.saugat.arbrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;


import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.ERENDER_SYSTEM;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.LLACoordinate;

import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.Screen;
import com.metaio.tools.io.AssetsManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class dynamic_location extends ARViewActivity implements SensorsComponentAndroid.Callback{


    private IRadar mRadar;

    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;
    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;

    private ArrayList<IGeometry> mPoi = new ArrayList<IGeometry>();
    private String poiResult;

    @Override
    protected int getGUILayout() {
        return R.layout.ar_test;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {

        mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
        mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
        mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);


        boolean result = metaioSDK.setTrackingConfiguration("GPS");

        metaioSDK.setLLAObjectRenderingLimits(10, 2000);

        metaioSDK.initializeRenderer(mSurfaceView.getWidth(), mSurfaceView.getHeight(),
                Screen.getRotation(this), ERENDER_SYSTEM.ERENDER_SYSTEM_OPENGL_ES_2_0 );
        mRendererInitialized = true;
        metaioSDK.setRendererClippingPlaneLimits(10, 500000);

        loadGPSContent();

    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry) {
        MetaioDebug.log("Geometry selected: " + geometry);

        mSurfaceView.queueEvent(new Runnable()
        {

            @Override
            public void run(){
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("yellow.png"));
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("red.png"));
                mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        InputStream is = null;
        String line;
        String res = null;


        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://192.168.1.150/arb/getLocation.php");
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

            Log.e("log_tag", "connection Success");

        }catch(Exception e){
            Log.e("Log_tag", "Error In Http Connection" + e.toString());
            Toast.makeText(getApplicationContext(),"Connection fail", Toast.LENGTH_SHORT).show();
        }

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();

            while((line = reader.readLine())!= null){
                builder.append(line);
            }

            set_poiResult(builder);


        }

        catch(Exception e){
            Log.e("log_tag", "Error parsing data" + e.toString());
        }

    }

    public void set_poiResult(StringBuilder builder){
        poiResult = builder.toString();
    }

    public String get_poiResult(){
        return poiResult;
    }

    public JSONArray get_poiDetails(String pois){
        JSONArray jArray = null;
        try {
            jArray = new JSONArray(pois);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jArray;
    }



    public void addPoi(IGeometry geometry){
        mPoi.add(geometry);
    }

    public ArrayList getPoi(){
        return mPoi;
    }



    public void loadGPSContent(){
        try{

            updateGeometries(mSensors.getLocation());

            mRadar = metaioSDK.createRadar();
            mRadar.setBackgroundTexture(AssetsManager.getAssetPath("radar.png"));
            mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("yellow.png"));
            mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);



            for(IGeometry geo:mPoi){
                mRadar.add(geo);
            }


        }
        catch(Exception e){
            MetaioDebug.log(Log.ERROR, "Error loading geometry");
        }
    }



    public void updateGeometries(LLACoordinate location){
        LLACoordinate currentLocation = location;
        try{
            JSONArray jArray = new JSONArray(get_poiResult());
            for (int i = 0; i < jArray.length()-1; i++) {
                JSONObject json_data = jArray.getJSONObject(i);
                Log.i("log_tag", "id: " + json_data.getInt("poiID") + ", POI-Name: " + json_data.getString("poiName"));
                LLACoordinate poi = new LLACoordinate(json_data.getDouble("poiLongitude"), json_data.getDouble("poiLatitude"), currentLocation.getAltitude(), currentLocation.getAccuracy());
                IGeometry geometry = createPoiGeomerty(poi);
                float distance = (float) MetaioCloudUtils.getDistanceBetweenTwoCoordinates(poi , mSensors.getLocation());
                mAnnotatedGeometriesGroup.addGeometry(geometry, json_data.getString("poiName") + distance);
                mPoi.add(geometry);

            }

        }catch(JSONException e){
            Log.e("log_tag", "Error parsing data" + e.toString());
            Toast.makeText(getApplicationContext(), "JsonArray fail", Toast.LENGTH_SHORT).show();
        }
    }

    private IGeometry createPoiGeomerty(LLACoordinate lla){
        String metaioMan = AssetsManager.getAssetPath("abcd.obj");

        if(metaioMan != null){
            IGeometry geo = metaioSDK.createGeometry(metaioMan);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(new Vector3d(100.0f, 100.0f, 100.0f));
            return geo;
        }
        else{
           return null;
        }
    }

    protected void onDestroy()
    {
        // Break circular reference of Java objects
        if (mAnnotatedGeometriesGroup != null)
        {
            mAnnotatedGeometriesGroup.registerCallback(null);
        }

        if (mAnnotatedGeometriesGroupCallback != null)
        {
            mAnnotatedGeometriesGroupCallback.delete();
            mAnnotatedGeometriesGroupCallback = null;
        }

        super.onDestroy();
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

    private String getAnnotationImageForTitle(String title) {
        Bitmap billboard = null;

        try {
            final String texturepath = getCacheDir() + "/" + title + ".png";
            Paint mPaint = new Paint();

            // Load background image and make a mutable copy

            String filepath = AssetsManager.getAssetPath("poi.png");
            Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);

            billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);

            Canvas c = new Canvas(billboard);

            mPaint.setColor(Color.WHITE);
            mPaint.setTextSize(24);
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setTextAlign(Paint.Align.CENTER);

            float y = 40 * 2;
            float x = 30 * 2;

            // Draw POI name
            if (title.length() > 0) {
                String n = title.trim();

                final int maxWidth = 160 * 2;

                int i = mPaint.breakText(n, true, maxWidth, null);

                int xPos = (c.getWidth() / 2);
                int yPos = (int) ((c.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));
                c.drawText(n.substring(0, i), xPos, yPos, mPaint);

                // Draw second line if valid
                if (i < n.length()) {
                    n = n.substring(i);
                    y += 20 * 2;
                    i = mPaint.breakText(n, true, maxWidth, null);

                    if (i < n.length()) {
                        i = mPaint.breakText(n, true, maxWidth - 20 * 2, null);
                        c.drawText(n.substring(0, i) + "...", x, y, mPaint);
                    } else {
                        c.drawText(n.substring(0, i), x, y, mPaint);
                    }
                }
            }

            // Write texture file
            try {
                FileOutputStream out = new FileOutputStream(texturepath);
                billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
                MetaioDebug.log("Texture file is saved to " + texturepath);
                return texturepath;
            } catch (Exception e) {
                MetaioDebug.log("Failed to save texture file");
                e.printStackTrace();
            }
        } catch (Exception e) {
            MetaioDebug.log("Error creating annotation texture: " + e.getMessage());
            MetaioDebug.printStackTrace(Log.DEBUG, e);
            return null;
        } finally {
            if (billboard != null) {
                billboard.recycle();
                billboard = null;
            }
        }

        return null;
    }

    final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback {

        @Override
        public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData,
                                               IGeometry existingAnnotation) {
            if (userData == null) {
                return null;
            }

            if (existingAnnotation != null) {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }

            String title = (String) userData; // as passed to addGeometry
            String texturePath = getAnnotationImageForTitle(title);

            return metaioSDK.createGeometryFromImage(texturePath, true, false);
        }
    }

}
