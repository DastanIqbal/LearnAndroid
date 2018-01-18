package com.dastanapps.camera2.listeners;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.TextureView;

import com.dastanapps.camera2.view.AutoFitTextureView;
import com.dastanapps.camera2.Camera2;
import com.dastanapps.camera2.Camera2Helper;

/**
 * Created by dastaniqbal on 18/01/2018.
 * dastanIqbal@marvelmedia.com
 * 18/01/2018 3:07
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CamSurfaceTextureListener implements TextureView.SurfaceTextureListener {

    private Camera2 camera2;
    private AutoFitTextureView mTextureView;
    private Activity mActivity;

    public CamSurfaceTextureListener(Camera2 camera2, AutoFitTextureView mTextureView, Activity mActivity) {
        this.camera2 = camera2;
        this.mTextureView = mTextureView;
        this.mActivity = mActivity;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        camera2.openCamera(width, height);
        camera2.setCameraWidthHeight();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                            int width, int height) {
        Camera2Helper.configureTransform(mActivity, camera2.getPreviewSize(), mTextureView, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}
