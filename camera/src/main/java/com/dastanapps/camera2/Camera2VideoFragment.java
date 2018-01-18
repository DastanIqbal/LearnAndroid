/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dastanapps.camera2;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.dastanapps.camera.R;
import com.dastanapps.view.AnimationImageView;
import com.dastanapps.view.FaceOverlayView;


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2VideoFragment extends Fragment
        implements View.OnClickListener, ICamera2, FragmentCompat.OnRequestPermissionsResultCallback, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "Camera2VideoFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            camera2.openCamera(width, height);
            camera2.setCameraWidthHeight();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            Camera2Helper.configureTransform(getActivity(), camera2.getPreviewSize(), mTextureView, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    public SurfaceView surfaceview;
    private Camera2 camera2;
    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;
    private Button mFlashButton;
    private SeekBar seekBar;
    private RadioGroup rb;
    private boolean isAE = true;

    public static Camera2VideoFragment newInstance() {
        return new Camera2VideoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        mTextureView = view.findViewById(R.id.texture);
        mButtonVideo = view.findViewById(R.id.video);
        mButtonVideo.setOnClickListener(this);

        // set the surface view for face detection
        surfaceview = view.findViewById(R.id.surfaceView);
        surfaceview.setZOrderOnTop(true);
        surfaceview.getHolder().setFormat(PixelFormat.TRANSPARENT); //for making it not visible on camera preview

        // Now create the OverlayView:
        mFaceView = new FaceOverlayView(getActivity());
        getActivity().addContentView(mFaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        mFlashButton = view.findViewById(R.id.btn_flash);
        mFlashButton.setOnClickListener(this);
        view.findViewById(R.id.btn_switch).setOnClickListener(this);
        view.findViewById(R.id.effects).setOnClickListener(this);
        view.findViewById(R.id.scenes).setOnClickListener(this);

        AnimationImageView mFocusImage = view.findViewById(R.id.img_focus);
        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        rb = view.findViewById(R.id.rb);
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.getCheckedRadioButtonId() == view.findViewById(R.id.ae).getId()) {
                    isAE = true;
                    seekBar.setMax(100);
                } else {
                    isAE = false;
                    seekBar.setMax(70);
                }
            }
        });

        camera2 = new Camera2(getActivity(), mTextureView, this);
        camera2.setFaceView(mFaceView);
        camera2.setFocusImage(mFocusImage);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            camera2.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        camera2.onResume();
    }

    @Override
    public void onPause() {
        camera2.onPause();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                camera2.toggleRecording();
                break;
            }
            case R.id.btn_flash:
                camera2.toggleFlash();
                break;
            case R.id.btn_switch:
                camera2.switchFaces();
                break;
            case R.id.effects:
                camera2.showEffectsDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
            case R.id.scenes:
                camera2.showScenesDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == DialogHelper.REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == DialogHelper.VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        DialogHelper.ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                DialogHelper.ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void cameraOperned(Size mPreviewSize) {
        if (null != mTextureView) {
            Camera2Helper.configureTransform(getActivity(), mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    @Override
    public void cameraError(int error) {
        if (error == 0) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            DialogHelper.ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    }

    @Override
    public void cameraRecordingStarted() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // UI
                mButtonVideo.setText(R.string.stop);
            }
        });
    }

    /**
     * Requests permissions needed for recording video.
     */
    @Override
    public void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(DialogHelper.VIDEO_PERMISSIONS)) {
            new DialogHelper.ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, DialogHelper.VIDEO_PERMISSIONS, DialogHelper.REQUEST_VIDEO_PERMISSIONS);
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cameraRecordingStopped() {
        mButtonVideo.setText(R.string.record);
    }

    @Override
    public void updateFlashMode(int flashMode) {
        if (flashMode == Camera2.FLASH_ON) {
            mFlashButton.setText("Flash On");
        } else if (flashMode == Camera2.FLASH_AUTO) {
            mFlashButton.setText("Flash Auto");
        } else {
            mFlashButton.setText("Flash Off");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isAE) {
            camera2.setAutoExposure(progress);
        } else {
            camera2.setWhiteBalance(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
