package com.dastanapps.learnandroid;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.dastanapps.learnandroid.databinding.ActivityMainBinding;
import com.dastanapps.learnandroid.shader.BitmapShaderA;
import com.dastanapps.learnandroid.shader.LinearGradientA;
import com.dastanapps.learnandroid.shader.PeekThroughImageViewA;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.menu_opts));
        activityMainBinding.list.setAdapter(menuAdapter);
        activityMainBinding.list.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                startActivity(LinearGradientA.class);
                break;
            case 1:
                startActivity(BitmapShaderA.class);
                break;
            case 2:
                startActivity(PeekThroughImageViewA.class);
                break;
        }
    }

    private void startActivity(Class clz) {
        startActivity(new Intent(this, clz));
    }
}
