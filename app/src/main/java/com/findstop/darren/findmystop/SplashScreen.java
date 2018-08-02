package com.findstop.darren.findmystop;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    private static int splash_finish = 3000; // variable to show screen for 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent splash = new Intent(SplashScreen.this, MainActivity.class); // Intent links the 2 java classes
                startActivity(splash); // Initiate the intent
                finish();
            }
        },splash_finish);
    }
}
