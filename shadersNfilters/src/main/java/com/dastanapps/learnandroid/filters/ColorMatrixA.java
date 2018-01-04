package com.dastanapps.learnandroid.filters;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.dastanapps.learnandroid.R;
import com.dastanapps.learnandroid.databinding.ActivityColorMatrixBinding;

public class ColorMatrixA extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityColorMatrixBinding activityColorMatrixBinding;
    private float[] grayScaleMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityColorMatrixBinding = DataBindingUtil.setContentView(this, R.layout.activity_color_matrix);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityColorMatrixBinding.spinr.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        activityColorMatrixBinding.imv.setImageResource(R.drawable.peek_image);
        Bitmap original = ((BitmapDrawable) activityColorMatrixBinding.imv.getDrawable()).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        switch (position) {
            case 0:
                paint.setColorFilter(new ColorMatrixColorFilter(getGrayScale()));
                break;
            case 1:
                paint.setColorFilter(new ColorMatrixColorFilter(getSepia()));
                break;
            case 2:
                paint.setColorFilter(new ColorMatrixColorFilter(getBinary()));
                break;
            case 3:
                paint.setColorFilter(new ColorMatrixColorFilter(getInvert()));
                break;
            case 4:
                paint.setColorFilter(new ColorMatrixColorFilter(getAlphaBlue()));
                break;
            case 5:
                paint.setColorFilter(new ColorMatrixColorFilter(getAlphaPink()));
                break;
        }
        canvas.drawBitmap(original, 0, 0, paint);

        activityColorMatrixBinding.imv.setImageBitmap(bitmap);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public ColorMatrix getGrayScale() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        return colorMatrix;

//        [ 0.213, 0.715, 0.072, 0, 0,
//            0.213, 0.715, 0.072, 0, 0,
//            0.213, 0.715, 0.072, 0, 0,
//            0,     0,     0, 1, 0 ]
//
//        // 0.213 + 0.715 + 0.072 = 1
    }

    public ColorMatrix getSepia() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrix colorScale = new ColorMatrix();
        colorScale.setScale(1, 1, 0.8f, 1);

        // Convert to grayscale, then apply brown color
        colorMatrix.postConcat(colorScale);

        return colorMatrix;

//        [ 1, 0,   0, 0, 0,
//          0, 1,   0, 0, 0,
//          0, 0, 0.8, 0, 0,
//          0, 0,   0, 1, 0 ]
    }

    public ColorMatrix getBinary() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        float m = 255f;
        float t = -255 * 128f;
        ColorMatrix threshold = new ColorMatrix(new float[]{
                m, 0, 0, 1, t,
                0, m, 0, 1, t,
                0, 0, m, 1, t,
                0, 0, 0, 1, 0
        });

        // Convert to grayscale, then scale and clamp
        colorMatrix.postConcat(threshold);

        return colorMatrix;
    }

    public ColorMatrix getInvert() {
        float m = 255f;
        ColorMatrix invertMatrix = new ColorMatrix(new float[]{
                -1, 0, 0, 0, m,
                0, -1, 0, 0, m,
                0, 0, -1, 0, m,
                0, 0, 0, 1, 0
        });

        return invertMatrix;
    }

    public ColorMatrix getAlphaBlue() {
        ColorMatrix alphaBlue = new ColorMatrix(new float[]{
                0, 0, 0, 0, 0,
                0.3f, 0, 0, 0, 50,
                0, 0, 0, 0, 255,
                0.2f, 0.4f, 0.4f, 0, -30
        });

        return alphaBlue;
    }

    private ColorMatrix getAlphaPink() {
        return new ColorMatrix(new float[]{
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 0,
                0.2f, 0, 0, 0, 50,
                0.2f, 0.2f, 0.2f, 0, -20
        });
    }

}
