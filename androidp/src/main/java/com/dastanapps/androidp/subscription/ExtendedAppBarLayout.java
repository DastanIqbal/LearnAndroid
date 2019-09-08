package com.dastanapps.androidp.subscription;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.dastanapps.androidp.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuo Liang on 11-Apr-18.
 */

public class ExtendedAppBarLayout extends AppBarLayout {

    private List<View> mChildViews = new ArrayList<>();
    private boolean mEnableAlphaScrolling;
    private Drawable[] mGradientBackgroundDrawables;
    private int previousBackgroundIndex = 0;

    public ExtendedAppBarLayout(Context context) {
        super(context);
    }

    public ExtendedAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExtendedAppBar);
            boolean isAlphaScrolling = ta.getBoolean(R.styleable.ExtendedAppBar_enableAlphaScrolling, false);
            int colorsId = ta.getResourceId(R.styleable.ExtendedAppBar_backgroundColors, 0);
            if (colorsId != 0) {
                TypedArray colorsArray = ta.getResources().obtainTypedArray(colorsId);
                int[] gradientBackgroundsResId = new int[colorsArray.length()];

                for (int i = 0; i < colorsArray.length(); i++) {
                    gradientBackgroundsResId[i] = colorsArray.getResourceId(i, 0);
                }
                setAnimationBackgroundGradients(gradientBackgroundsResId);
                colorsArray.recycle();
            }

            enableAlphaScrolling(isAlphaScrolling);
            ta.recycle();
        }
    }

    public void animateBackground(int index) {
        if (mGradientBackgroundDrawables == null || index >= mGradientBackgroundDrawables.length) {
            return;
        }
        Drawable targetDrawable = mGradientBackgroundDrawables[index];
        Drawable previousDrawable = mGradientBackgroundDrawables[previousBackgroundIndex];
        final TransitionDrawable anim = new TransitionDrawable(new Drawable[] {
                previousDrawable,
                targetDrawable
        });
        anim.setCrossFadeEnabled(true);
        previousBackgroundIndex = index;
        post(() -> {
            setBackground(anim);
            anim.startTransition(1000);
        });
    }

    protected void addChildren(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof CollapsingToolbarLayout) {
                addChildren((CollapsingToolbarLayout)child);
            } else {
                mChildViews.add(child);
            }
        }
    }

    public void setChildrenAlpha(float alpha) {
        for (View view: mChildViews) {
            view.setAlpha(alpha);
        }
    }

    public void enableAlphaScrolling(boolean enabled) {
        mEnableAlphaScrolling = enabled;
    }

    public void setAnimationBackgroundGradients(Drawable[] drawables) {
        if (drawables == null) {
            throw new NullPointerException("Drawable must not be null");
        }
        mGradientBackgroundDrawables = drawables;
    }

    public void setAnimationBackgroundGradients(int[] drawableIds) {
        mGradientBackgroundDrawables = new Drawable[drawableIds.length];
        for (int i = 0; i < drawableIds.length; i++) {
            mGradientBackgroundDrawables[i] = getResources().getDrawable(drawableIds[i]);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        addChildren(this);
        addOnOffsetChangedListener(mOnOffsetChangedListener);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mChildViews.clear();
        removeOnOffsetChangedListener(mOnOffsetChangedListener);
        super.onDetachedFromWindow();
    }

    OnOffsetChangedListener mOnOffsetChangedListener = (appBarLayout, verticalOffset) -> {
        float total = (float)appBarLayout.getTotalScrollRange();
        float percentage = (total + verticalOffset) / total;
        if (mEnableAlphaScrolling) {
            setChildrenAlpha(percentage);
        }
    };

}
