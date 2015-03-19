package com.saugat.arbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread splashScreen = new Thread(){
            public void run(){
                try{
                    sleep(2000);

                    Intent i = new Intent(getBaseContext(),HomeActivity.class);
                    startActivity(i);
                }catch(Exception e){

                }
            }
        };

        splashScreen.start();

    }

    protected void onDestroy(){
        super.onDestroy();
    }
}
