package com.dastanapps.camera2.Preview.CameraSurface;

import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.View;

import com.dastanapps.camera2.CameraController.CameraController;
import com.dastanapps.camera2.opengles.CameraSurfaceRenderer;


/** Provides support for the surface used for the preview - this can either be
 *  a SurfaceView or a TextureView.
 */
public interface CameraSurface {
	View getView();
	void setPreviewDisplay(CameraController camera_controller); // n.b., uses double-dispatch similar to Visitor pattern - behaviour depends on type of CameraSurface and CameraController
	void setVideoRecorder(MediaRecorder video_recorder);
	void setTransform(Matrix matrix);
	void onPause();
	void onResume();
	void setEGLSurfaceTextureListener(CameraSurfaceRenderer.EGLSurfaceTextureListener eglSurfaceTextureListener);
}
