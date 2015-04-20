package com.saugat.arbrowser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.metaio.sdk.jni.IGeometry;


public class databaseTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_retrieve_test);
        MySQLiteHelper db = new MySQLiteHelper(this);


        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String result = null;
        InputStream is = null;
        String line;

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

            result = builder.toString();

        }

        catch(Exception e){

        }

        try {
            JSONArray jArray = new JSONArray(result);


            TableLayout tv = (TableLayout) findViewById(R.id.table);
            tv.removeAllViewsInLayout();

            for (int i = 0; i < jArray.length()-1; i++) {
               JSONObject json_data = jArray.getJSONObject(i);
//             Log.i("log_tag", "id: " + json_data.getInt("poi_id") + ", Username: " + json_data.getString("poi_name") + ", No: " + json_data.getDouble("poi_longitude"));

                Log.d("Insert: ", "Inserting ..");
                db.createPoi(new poi(Integer.parseInt(json_data.getString("poiID")),
                        json_data.getString("poiName"),
                        json_data.getDouble("poiLongitude"),
                        json_data.getDouble("poiLatitude")
                ));
             }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data" + e.toString());
            Toast.makeText(getApplicationContext(), "JsonArray fail", Toast.LENGTH_SHORT).show();
        }

        Log.d("Reading: ", "Reading all contacts..");
        ArrayList<IGeometry> allPoi = db.getAllPoi();

    }

}
