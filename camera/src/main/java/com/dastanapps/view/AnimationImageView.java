package com.dastanapps.view;

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
public class AnimationImageView extends AppCompatImageView {
    private Animation mAnimation;
    private Context mContext;
    /**
     * 防止又换了个text，但是上次哪个还没有消失即将小时就把新的text的给消失了
     */
    public int mTimes = 0;

    public AnimationImageView(Context context) {
        this(context, null);
        mContext = context;
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mContext = context;
        mAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setDuration(200);
        this.setVisibility(VISIBLE);
    }

    public void startFocusing() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.startAnimation(mAnimation);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus));
    }

    public void focusFailed() {
        mTimes++;
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_failed));
    }

    public void focusSuccess() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_succeed));
    }

    public void stopFocus() {
        this.setVisibility(INVISIBLE);
    }
}
