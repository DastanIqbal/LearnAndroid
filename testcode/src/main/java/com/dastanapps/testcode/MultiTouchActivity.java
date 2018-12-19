package com.dastanapps.testcode;

import android.app.Activity;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MultiTouchActivity extends Activity{

    private View imageView;
    float dX, dY;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 5.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //imageView = findViewById(R.id.fl);
        //imageView.setOnTouchListener(this);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    private boolean scaling = false;

    /*@Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        if (event.getPointerCount() > 1) {
            scaling = true;
            mScaleGestureDetector.onTouchEvent(event);
            Logger.INSTANCE.onlyDebug("ScaleGesture TouchEvent");
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    scaling = false;
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    Logger.INSTANCE.onlyDebug("ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mScaleGestureDetector.isInProgress() && !scaling)
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                    Logger.INSTANCE.onlyDebug("ACTION_MOVE");
                    break;
                default:
                    Logger.INSTANCE.onlyDebug("Action: " + event.getAction());
                    return false;
            }
        }
        return true;
    }*/

}