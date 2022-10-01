package com.example.lab1;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ActivitySettings extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_container);
        getLayoutInflater().inflate(R.layout.activity_settings, contentFrameLayout);
    }
}
