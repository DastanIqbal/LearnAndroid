package com.dastanapps.gles;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.dastanapps.gles.filters.CameraFilter;
import com.dastanapps.gles.filters.NoneFilter;

/**
 * Default camera renderer that simply draws a quad with the camera texture.
 */
public class DefaultCameraRenderer implements TextureViewGLWrapper.GLRenderer {
    private final Context context;
    private CameraFilter cameraFilter;
    private int surfaceWidth;
    private int surfaceHeight;
    private int texId;

    public DefaultCameraRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture eglSurfaceTexture, int surfaceWidth, int surfaceHeight) {
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
        cameraFilter = new NoneFilter();
        texId = GLUtils.Companion.initTexture();
    }

    @Override
    public void onSurfaceChanged(SurfaceTexture eglSurfaceTexture, int surfaceWidth, int surfaceHeight) {
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture eglSurfaceTexture) {
        //Update camera parameters
        cameraFilter.draw(surfaceWidth, surfaceHeight, eglSurfaceTexture, texId);
    }

    @Override
    public void onSurfaceDestroyed(SurfaceTexture eglSurfaceTexture) {
        //We have nothing to dispose
        cameraFilter.release();
    }

    @Override
    public void changeFilter(final CameraFilter filter) {
        cameraFilter = filter;
    }
}
