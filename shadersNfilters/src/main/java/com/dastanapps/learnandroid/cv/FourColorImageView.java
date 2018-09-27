package com.dastanapps.learnandroid.cv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by dastaniqbal on 30/04/2017.

 * 30/04/2017 9:11
 */

public class FourColorImageView extends AppCompatImageView {
    private Bitmap bitmap = null;

    public FourColorImageView(Context context) {
        super(context);
    }

    public FourColorImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FourColorImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) {
            Bitmap quarter = Bitmap.createBitmap(getWidth() , getHeight() , Bitmap.Config.ARGB_8888);
            Canvas quarterCanvas = new Canvas(quarter);
            quarterCanvas.scale(0.5f, 0.5f);
            super.onDraw(quarterCanvas);

            createBitmap(quarter);
            quarter.recycle();
        }

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void createBitmap(Bitmap quarter) {
        bitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        // Top left
        paint.setColorFilter(new LightingColorFilter(Color.RED, 0));
        canvas.drawBitmap(quarter, 0, 0, paint);

        // Top right
        paint.setColorFilter(new LightingColorFilter(Color.YELLOW, 0));
        canvas.drawBitmap(quarter, getWidth()/2, 0, paint);

        // Bottom left
        paint.setColorFilter(new LightingColorFilter(Color.BLUE, 0));
        canvas.drawBitmap(quarter, 0, getHeight()/2, paint);

        // Bottom right
        paint.setColorFilter(new LightingColorFilter(Color.GREEN, 0));
        canvas.drawBitmap(quarter, getWidth()/2, getHeight()/2, paint);
    }
}
