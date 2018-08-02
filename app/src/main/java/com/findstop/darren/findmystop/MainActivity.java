package com.findstop.darren.findmystop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static Button button_sbm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnClickButtonListener();
    }

    public void OnClickButtonListener() { // Button linked to a new activity via intent
        button_sbm = (Button)findViewById(R.id.button);
        button_sbm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentone = new Intent("com.findstop.darren.findmystop.ChooseService");
                        startActivity(intentone);
                    }
                }
        );

    }

}

