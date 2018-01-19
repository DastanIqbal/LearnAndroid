package com.dastanapps.camera.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.dastanapps.camera.R;

/**
 * Extends View. Just used to draw Rect when the screen is touched
 * for auto focus.
 * <p>
 * Use setHaveTouch function to set the status and the Rect to be drawn.
 * Call invalidate to draw Rect. Call invalidate again after
 * setHaveTouch(false, Rect(0, 0, 0, 0)) to hide the rectangle.
 */
public class FocusImageView extends AppCompatImageView {
    private Context mContext;
    private Animation mAnimation;
    private int mRawX;
    private int mRawY;

    public FocusImageView(Context context) {
        this(context, null);
    }

    public FocusImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setDuration(200);
        this.setVisibility(VISIBLE);
    }

    public void startFocusing(MotionEvent event) {
        int width = getWidth();
        int height = getHeight();
        if (event != null && mRawX != event.getX() && mRawY != event.getY()) {
            mRawX = (int) event.getX();
            mRawY = (int) event.getY();
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(getLayoutParams());
            margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
            setLayoutParams(layoutParams);
        }
        this.setVisibility(View.VISIBLE);
        this.startAnimation(mAnimation);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus));
    }

    public void focusFailed() {
        //this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_failed));
        stopFocus();
    }

    public void focusSuccess() {
        this.setVisibility(View.VISIBLE);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_succeed));
    }

    public void stopFocus() {
        this.setVisibility(INVISIBLE);
    }
}