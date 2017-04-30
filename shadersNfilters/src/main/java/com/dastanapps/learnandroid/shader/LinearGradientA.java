package com.dastanapps.learnandroid.shader;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dastanapps.learnandroid.R;
import com.dastanapps.learnandroid.databinding.ActivityLinearGradientBinding;

public class LinearGradientA extends AppCompatActivity {


    ActivityLinearGradientBinding activityLinearGradientBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLinearGradientBinding = DataBindingUtil.setContentView(this, R.layout.activity_linear_gradient);
    }
}
