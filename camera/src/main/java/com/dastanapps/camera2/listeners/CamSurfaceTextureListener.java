package com.dastanapps.camera2.listeners;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.TextureView;

import com.dastanapps.camera2.Camera2;
import com.dastanapps.camera2.view.Cam2AutoFitTextureView;
import com.dastanapps.gles.TextureViewGLWrapper;

/**
 * Created by dastaniqbal on 18/01/2018.

 * 18/01/2018 3:07
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CamSurfaceTextureListener implements TextureView.SurfaceTextureListener {

    private Camera2 camera2;
    private Cam2AutoFitTextureView mTextureView;
    private Activity mActivity;
    private TextureViewGLWrapper filterTextureGL;

    public CamSurfaceTextureListener(Camera2 camera2, Cam2AutoFitTextureView mTextureView, Activity mActivity) {
        this.camera2 = camera2;
        this.mTextureView = mTextureView;
        this.mActivity = mActivity;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        //camera2.openCamera(width, height);
        filterTextureGL.onSurfaceTextureAvailable(surfaceTexture, width, height);
        camera2.setCameraWidthHeight();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        //Camera2Helper.configureTransform(mActivity, camera2.getPreviewSize(), mTextureView, width, height);
        filterTextureGL.onSurfaceTextureSizeChanged(surfaceTexture, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        filterTextureGL.onSurfaceTextureDestroyed(surfaceTexture);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        filterTextureGL.onSurfaceTextureUpdated(surfaceTexture);
    }

    public void setFilterTextureGL(TextureViewGLWrapper filterTextureGL) {
        this.filterTextureGL = filterTextureGL;
    }
}
