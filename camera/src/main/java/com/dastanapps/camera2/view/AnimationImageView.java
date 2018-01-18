package com.dastanapps.camera2.view;

import android.content.Context;
import android.os.Handler;
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
    private Handler mHandler;

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
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void startFocusing() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
                startAnimation(mAnimation);
                setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus));
            }
        });
    }

    public void focusFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_failed));
            }
        });
    }

    public void focusSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
                setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_succeed));
            }
        });
    }

    public void stopFocus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(INVISIBLE);
            }
        });
    }
}
