package com.saugat.arbrowser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.metaio.sdk.jni.IGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DArKLoRD on 4/20/2015.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "arb";
    private static final String TABLE_ARB = "arb_poi";

    public static final String KEY_POI_ID = "id";
    public static final String KEY_POI_NAME = "name";
    public static final String KEY_POI_LONGITUDE = "poiLongitude";
    public static final String KEY_POI_LATITUDE = "poiLatitude";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_ARB
            + "(" + KEY_POI_ID + " integer primary key, "
            + KEY_POI_NAME + " text not null,"
            + KEY_POI_LONGITUDE + " double not null,"
            + KEY_POI_LATITUDE + " double not null);";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARB);
        onCreate(db);
    }


    public void createPoi(poi poi){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_POI_ID, poi.getPoiId());
        values.put(KEY_POI_NAME, poi.getPoiName());
        values.put(KEY_POI_LONGITUDE, poi.getPoiLongitude());
        values.put(KEY_POI_LATITUDE, poi.getPoiLatitude());

        db.insert(TABLE_ARB, null, values);
        db.close();
    }

    public ArrayList<IGeometry> getAllPoi(){
        ArrayList<IGeometry> mPoi = new ArrayList<IGeometry>();
        String selectQuery = "SELECT poiName FROM" + TABLE_ARB ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                poi poi = new poi();




            } while (cursor.moveToNext(

            ));
        }

        return mPoi;
    }
}
