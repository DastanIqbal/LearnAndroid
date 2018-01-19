package com.dastanapps.camera2.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.dastanapps.camera.R;


/**
 * Created by yuyidong on 14-12-23.
 */
public class FocusImageView extends AppCompatImageView {
    private Animation mAnimation;
    private Context mContext;
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

    public void startFocusing() {
        this.setVisibility(View.VISIBLE);
        this.startAnimation(mAnimation);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus));
    }

    public void focusFailed() {
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_failed));
    }

    public void focusSuccess() {
        this.setVisibility(View.VISIBLE);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_succeed));
    }

    public void stopFocus() {
        this.setVisibility(INVISIBLE);
    }
}
