package com.example.myapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //Value: tesla = 0, BMW = 1, audi = 2

    private String radioButtonSelectionValue = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button ServiceButton = (Button) findViewById(R.id.proceedToMaps);
        ServiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OpenMapsActivity();
            }
        });

        RadioButton teslaRadioButton = findViewById(R.id.TeslaRadioButton);
        teslaRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonSelectionValue = "0";
            }
        });

        RadioButton BMWRadioButton = findViewById(R.id.BMWRadioButton);
        BMWRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonSelectionValue = "1";
            }
        });

        RadioButton audiRadioButton = findViewById(R.id.AudiRadioButton);
        audiRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonSelectionValue = "2";
            }
        });

    }


    public void OpenMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("RadioButtonChosen", radioButtonSelectionValue);
        startActivity(intent);
    }

}
