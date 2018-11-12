package com.example.nahue.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainButton extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Inicio");
    }
    public void goToAnActivity(View view) {
        Intent Intent = new Intent(this,Bluetooth.class);
                startActivity(Intent);
    }


}

