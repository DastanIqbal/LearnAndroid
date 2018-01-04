package com.dastanapps.mediaeffectswithopengles;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.dastanapps.mediaeffectswithopengles.databinding.ActivityMainBinding;
import com.dastanapps.mediaeffectswithopengles.record.GLSurfaceViewPlayerActivity;
import com.dastanapps.mediaeffectswithopengles.record.RecordGLwithoutRenderOnScreen;
import com.dastanapps.mediaeffectswithopengles.record.RecordSurfaceActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.menu_opts));
        activityMainBinding.list.setAdapter(menuAdapter);
        activityMainBinding.list.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                RecordGLwithoutRenderOnScreen recordGLwithoutRenderOnScreen = new RecordGLwithoutRenderOnScreen();
                recordGLwithoutRenderOnScreen.recordOpenGLVideo();
                Toast.makeText(this, "Search test.currentDate.mp4 file in File Storage", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                startActivity(RecordSurfaceActivity.class);
                break;
            case 2:
                startActivity(GLSurfaceViewPlayerActivity.class);
                break;
        }
    }

    private void startActivity(Class clz) {
        startActivity(new Intent(this, clz));
    }
}
