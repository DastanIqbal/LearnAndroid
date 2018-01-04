package com.dastanapps.learnandroid.shader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dastanapps.learnandroid.R;

public class PeekThroughImageViewA extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peak_through_image_view);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
