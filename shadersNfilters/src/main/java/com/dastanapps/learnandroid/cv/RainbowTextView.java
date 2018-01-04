package com.dastanapps.learnandroid.cv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.dastanapps.learnandroid.R;

/**
 * TODO: document your custom view class.
 */
public class RainbowTextView extends AppCompatTextView {
    private String tileMode = "mirror";
    private Context context;

    public RainbowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RainbowTextView);
        tileMode = typedArray.getString(R.styleable.RainbowTextView_tilemode);
        typedArray.recycle();
    }

    public RainbowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RainbowTextView, defStyleAttr, 0);
        tileMode = typedArray.getString(R.styleable.RainbowTextView_tilemode);
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Shader shader = new LinearGradient(0, 0, 0, w, getRainbowColors(), null, getTileMode());

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        shader.setLocalMatrix(matrix);
        getPaint().setShader(shader);
    }

    private int[] getRainbowColors() {
        return new int[]{
                ContextCompat.getColor(context, R.color.rainbow_violet),
                ContextCompat.getColor(context, R.color.rainbow_indigo),
                ContextCompat.getColor(context, R.color.rainbow_blue),
                ContextCompat.getColor(context, R.color.rainbow_green),
                ContextCompat.getColor(context, R.color.rainbow_yellow),
                ContextCompat.getColor(context, R.color.rainbow_red)
        };
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
