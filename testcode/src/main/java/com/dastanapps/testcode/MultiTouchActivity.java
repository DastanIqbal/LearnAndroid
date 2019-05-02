package com.dastanapps.testcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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
            String stringBuilder = "\nCanvas " + fl.getMeasuredWidth() + ", " + fl.getMeasuredHeight() + "\n";
            tv.setText(stringBuilder);
            Rect local = new Rect();
            view.getLocalVisibleRect(local);
//            stringBuilder=rect1.flattenToString();
//            tv.append("Visible Portion Local: ");
//            tv.append(stringBuilder);

            Rect global = new Rect();
            Point point = new Point();
            boolean isVisible = view.getGlobalVisibleRect(global, point);
            if (!isVisible) {
                tv.append("\n View not vissible \n");
                tv.setTextColor(Color.RED);
            } else {
                // global.offset(-point.x, -point.y);
                tv.append("\n Global ");
                tv.append(global.flattenToString());
                tv.append("\n Point " + point.x + " : " + point.y);
                tv.append("\n Width,Height " + global.width() + " : " + global.height());
                tv.setTextColor(Color.BLACK);
            }

//            view.getWindowVisibleDisplayFrame(rect1);
//            tv.append("\n WindowVisible ");
//            tv.append(rect1.flattenToString());

            stringBuilder = "\n\n X=" + x +
                    ", Y=" + y +
                    " Width=" + width +
                    ", Height=" + height;
            tv.append(stringBuilder);

            Log.d("MTA", tv.getText().toString());

        });
    }
}