package com.dastanapps.learnandroid.shader.cv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.dastanapps.learnandroid.R;

/**
 * TODO: document your custom view class.
 */
public class BitmapShaderTextView extends AppCompatTextView {
    private Context context;
    private String tileMode = "mirror";

    public BitmapShaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BitmapShaderTextView);
        tileMode = typedArray.getString(R.styleable.BitmapShaderTextView_bmptilemode);
        typedArray.recycle();
    }

    public BitmapShaderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BitmapShaderTextView, defStyleAttr, 0);
        tileMode = typedArray.getString(R.styleable.BitmapShaderTextView_bmptilemode);
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tile_20);
        Shader shader = new BitmapShader(bitmap, getTileMode(), getTileMode());
        getPaint().setShader(shader);
    }

    public Shader.TileMode getTileMode() {
        if (tileMode.equals("clamp")) {
            return Shader.TileMode.CLAMP;
        } else if (tileMode.equals("repeat")) {
            return Shader.TileMode.REPEAT;
        }
        return Shader.TileMode.MIRROR;
    }
}
