package com.findstop.darren.findmystop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class ChooseService extends AppCompatActivity {

    private Spinner spinner1, spinner2;
    private Button btnSubmit;
    private Switch newSwitch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_service);

        addItemsOnSpinner1();
        addListenerOnButton();

    }


    public void addItemsOnSpinner1() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        List<String> spinlist1 = new ArrayList<String>();
        spinlist1.add("Choose Service here...");
        spinlist1.add("563 Jordanstown to Belfast");
        spinlist1.add("210 Cookstown - Belfast");
        spinlist1.add("215 Belfast - Downpatrick");
        spinlist1.add("7a City Centre - Four Winds");



        ArrayAdapter<String> spin1Adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinlist1);
        spin1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(spin1Adapter);
    }





    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        newSwitch = (Switch) findViewById(R.id.busswitch);


        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(String.valueOf(spinner1.getSelectedItem()) == "563 Jordanstown to Belfast" && newSwitch.isChecked()){

                    Intent intentone = new Intent("com.findstop.darren.findmystop.FindStops");
                    startActivity(intentone);

                }else if(String.valueOf(spinner1.getSelectedItem()) == "7a City Centre - Four Winds" && newSwitch.isChecked()) {

                    Intent intenttwo = new Intent("com.findstop.darren.findmystop.sevenAInward");
                    startActivity(intenttwo);

                }else if(String.valueOf(spinner1.getSelectedItem()) == "7a City Centre - Four Winds") {

                    Intent intentthree = new Intent("com.findstop.darren.findmystop.sevenAOutbound");
                    startActivity(intentthree);

                }else if(String.valueOf(spinner1.getSelectedItem()) == "563 Jordanstown to Belfast") {

                    Intent intentfour = new Intent("com.findstop.darren.findmystop.jtownOutbound");
                    startActivity(intentfour);

                }else if(String.valueOf(spinner1.getSelectedItem()) == "210 Cookstown - Belfast") {

                    Toast.makeText(ChooseService.this, "This service has not been added yet", Toast.LENGTH_LONG).show();

                }else if(String.valueOf(spinner1.getSelectedItem()) ==  "215 Belfast - Downpatrick") {

                    Toast.makeText(ChooseService.this, "This service has not been added yet", Toast.LENGTH_LONG).show();

                }else {
                    Toast.makeText(ChooseService.this, "No service was selected", Toast.LENGTH_LONG).show();


                }
            }

        });
    }
}
