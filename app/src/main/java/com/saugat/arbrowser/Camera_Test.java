package com.saugat.arbrowser;

import java.io.File;
import java.util.concurrent.locks.Lock;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.ERENDER_SYSTEM;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.Screen;
import com.metaio.tools.io.AssetsManager;

public class Camera_Test extends ARViewActivity implements SensorsComponentAndroid.Callback{

    private IRadar mRadar;

    private IGeometry mHandBallGround;
    private IGeometry mGaneshMandir;
    private IGeometry mRandom;
    private IGeometry mIslingtonCollege;
    private IGeometry mBritHouse;

    LLACoordinate handBallGround;
    LLACoordinate ganeshMandir;
    LLACoordinate random;
    LLACoordinate islingtonCollege;
    LLACoordinate britHouse;


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
        boolean result = metaioSDK.setTrackingConfiguration("GPS");

        metaioSDK.setLLAObjectRenderingLimits(10, 1000);

        metaioSDK.initializeRenderer(mSurfaceView.getWidth(), mSurfaceView.getHeight(),
                Screen.getRotation(this), ERENDER_SYSTEM.ERENDER_SYSTEM_OPENGL_ES_2_0 );
        mRendererInitialized = true;
        metaioSDK.setRendererClippingPlaneLimits(10, 500000);

        loadGPSContent();

    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void loadGPSContent(){
        try{
            String metaioMan = AssetsManager.getAssetPath("metaioman.md2");

//            IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup = null;
//            mAnnotatedGeometriesGroup.addGeometry(mIslingtonCollege, "Islington College");
//            mAnnotatedGeometriesGroup.addGeometry(mHandBallGround, "Bla bal");
//            mAnnotatedGeometriesGroup.addGeometry(mBritHouse, "Brit House");



            mHandBallGround = metaioSDK.createGeometry(metaioMan);
            mIslingtonCollege = metaioSDK.createGeometry(metaioMan);
            mBritHouse = metaioSDK.createGeometry(metaioMan);


            updateGeometries(mSensors.getLocation());

            mRadar = metaioSDK.createRadar();
            mRadar.setBackgroundTexture(AssetsManager.getAssetPath("radar.png"));
            mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("yellow.png"));
            mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);


            mRadar.add(mHandBallGround);
            mRadar.add(mIslingtonCollege);
            mRadar.add(mBritHouse);
        }
        catch(Exception e){
            MetaioDebug.log(Log.ERROR, "Error loading geometry");
        }
    }

    public void updateGeometries(LLACoordinate location){

        LLACoordinate currentPosition = location;
        handBallGround = new LLACoordinate(27.70835172, 85.32576665, currentPosition.getAltitude(), currentPosition.getAccuracy());
        islingtonCollege = new LLACoordinate(27.70762152, 85.32510817, currentPosition.getAltitude(), currentPosition.getAccuracy());
        britHouse = new LLACoordinate(27.7081873, 85.3238916, currentPosition.getAltitude(), currentPosition.getAccuracy());

        if(handBallGround != null){
            mHandBallGround.setTranslationLLA(handBallGround);
            mHandBallGround.setLLALimitsEnabled(true);
            mHandBallGround.setScale((new Vector3d(100.0f, 100.0f, 100.0f)));
        }

        if(islingtonCollege != null){
            mIslingtonCollege.setTranslationLLA(islingtonCollege);
            mIslingtonCollege.setLLALimitsEnabled(true);
            mIslingtonCollege.setScale((new Vector3d(100.0f, 100.0f, 100.0f)));
        }

        if(britHouse != null){
            mBritHouse.setTranslationLLA(britHouse);
            mBritHouse.setLLALimitsEnabled(true);
            mBritHouse.setScale((new Vector3d(100.0f, 100.0f, 100.0f)));
        }
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
}
