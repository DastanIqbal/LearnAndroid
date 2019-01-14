package com.dastanapps.testcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dastanapps.customview.MultiTouchView;

public class MultiTouchActivity extends Activity {

    private MultiTouchView view;
    private TextView tv;
    private FrameLayout fl;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fl = findViewById(R.id.fl);
        view = findViewById(R.id.mtv);
        tv = findViewById(R.id.tv);

        view.setPositionChangeListener((x, y, width, height) -> {
            tv.setText("Canvas " + fl.getMeasuredWidth() + ", " + fl.getMeasuredHeight() + "\n");
            String stringBuilder = "X=" + x +
                    ", Y=" + y +
                    " Width=" + width +
                    ", Height=" + height;
            tv.append(stringBuilder);

        });
    }
}