package com.dastanapps.learnandroid.filters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dastanapps.learnandroid.R;

public class FourColorImageViewA extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_color_image_view);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
