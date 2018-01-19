package com.dastanapps.camera.listeners;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

import com.dastanapps.camera.Camera1;
import com.dastanapps.view.AutoFitTextureView;

public class Cam1SurfaceTextureListener implements TextureView.SurfaceTextureListener {
    private Camera1 camera1;
    private AutoFitTextureView mTextureView;
    private Activity mActivity;

    public Cam1SurfaceTextureListener(Camera1 camera1, AutoFitTextureView mTextureView, Activity activity) {
        this.camera1 = camera1;
        this.mTextureView = mTextureView;
        this.mActivity = activity;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                            int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera1.closeCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("!!!!", "onSurfaceTextureAvailable!!!");
        camera1.openCamera();
    }
}