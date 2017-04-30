package com.dastanapps.learnandroid.shader;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dastanapps.learnandroid.R;
import com.dastanapps.learnandroid.databinding.ActivityBitmapShaderBinding;

public class BitmapShaderA extends AppCompatActivity {

    ActivityBitmapShaderBinding activityBitmapShaderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBitmapShaderBinding = DataBindingUtil.setContentView(this, R.layout.activity_bitmap_shader);
    }
}
