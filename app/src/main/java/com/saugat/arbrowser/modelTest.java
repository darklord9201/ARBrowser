package com.saugat.arbrowser;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;


public class modelTest extends ARViewActivity {

    private String mTrackingFile;
    private IGeometry mMan;

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
        mTrackingFile = AssetsManager.getAssetPath("TrackingData_MarkerlessFast.xml");
        boolean result = metaioSDK.setTrackingConfiguration(mTrackingFile);
        MetaioDebug.log("Tracking data loaded" + result);

        String modelPath = AssetsManager.getAssetPath("metaioman.md2");

        if(modelPath != null){
            mMan = metaioSDK.createGeometry(modelPath);
            if(mMan != null){
                mMan.setScale(new Vector3d(4.0f, 4.0f, 4.0f));
                mMan.setVisible(true);
                MetaioDebug.log("Loaded geometry" + modelPath);
            }
            else{
                MetaioDebug.log(Log.ERROR,"Error Loading Geometry" + modelPath );
            }
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}
