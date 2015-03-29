package com.saugat.arbrowser;

import java.io.File;
import java.util.concurrent.locks.Lock;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

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

        LLACoordinate islingtonCollege = new LLACoordinate(27.7079649, 85.326495, 0, 0);
        LLACoordinate home = new LLACoordinate(27.7366866, 85.357272, 0, 0);
        mIslingtonCollege = createPOIGeometry(islingtonCollege);
        mHome = createPOIGeometry(home);






        String metaioManModel = AssetsManager.getAssetPath("metaioman.md2");

        if(metaioManModel != null){
            mIslingtonCollege = metaioSDK.createGeometry(metaioManModel);
            if(mIslingtonCollege != null){
                mIslingtonCollege.setTranslationLLA(islingtonCollege);
                mIslingtonCollege.setScale(200);
            }
            else{
                MetaioDebug.log(Log.ERROR, "Error loading geometry" + metaioManModel);
            }
        }

        mRadar = metaioSDK.createRadar();
        mRadar.setBackgroundTexture(AssetsManager.getAssetPath("radar.png"));
        mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("yellow.png"));
        mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

        mRadar.add(mIslingtonCollege);
        mRadar.add(mHome);
    }

    private IGeometry createPOIGeometry(LLACoordinate lla){

        String path = AssetsManager.getAssetPath("metaioman.md2");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(100);
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

    }
}
