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

package com.dastanapps.camera;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.dastanapps.camera.view.Cam1AutoFitTextureView;
import com.dastanapps.camera.view.FocusImageView;


public class Camera1VideoFragment extends Fragment
        implements View.OnClickListener, ICamera1, FragmentCompat.OnRequestPermissionsResultCallback, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "Camera2VideoFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * An {@link Cam1AutoFitTextureView} for camera preview.
     */
    private Cam1AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    public SurfaceView surfaceview;
    private Camera1 camera1;
    private Button mFlashButton;
    private SeekBar seekBar;
    private RadioGroup rb;
    private boolean isAE = true;

    public static Camera1VideoFragment newInstance() {
        return new Camera1VideoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera1_video, container, false);
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


        mFlashButton = view.findViewById(R.id.btn_flash);
        mFlashButton.setOnClickListener(this);
        view.findViewById(R.id.btn_switch).setOnClickListener(this);
        view.findViewById(R.id.effects).setOnClickListener(this);
        view.findViewById(R.id.scenes).setOnClickListener(this);
        view.findViewById(R.id.whitebalance).setOnClickListener(this);

        FocusImageView mFocusImage = view.findViewById(R.id.img_focus);
        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(this);

        rb = view.findViewById(R.id.rb);
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.getCheckedRadioButtonId() == view.findViewById(R.id.ae).getId()) {
                    isAE = true;
                    seekBar.setProgress(50);
                    seekBar.setVisibility(View.VISIBLE);
                } else {
                    isAE = false;
                    seekBar.setVisibility(View.GONE);
                }
            }
        });

        camera1 = new Camera1(getActivity(), mTextureView);
//        camera1.setFaceView(mFaceView);
        camera1.setFocusImage(mFocusImage);

    }

    @Override
    public void onResume() {
        super.onResume();
        camera1.onResume();
    }

    @Override
    public void onPause() {
        camera1.onPause();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                //camera1.toggleRecording();
                break;
            }
            case R.id.btn_flash:
                camera1.toggleFlash().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
            case R.id.btn_switch:
                camera1.switchFaces();
                break;
            case R.id.effects:
                camera1.showEffectsDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
            case R.id.scenes:
                camera1.showScenesDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
            case R.id.whitebalance:
                camera1.showWhiteBalance().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                break;
        }
    }

    @Override
    public void cameraOperned(Size mPreviewSize) {
        if (null != mTextureView) {
            //Camera2Helper.configureTransform(getActivity(), mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
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

    @Override
    public void cameraRecordingStopped() {
        mButtonVideo.setText(R.string.record);
    }

    @Override
    public void updateFlashMode(int flashMode) {
//        if (flashMode == Camera2.FLASH_ON) {
//            mFlashButton.setText("Flash On");
//        } else if (flashMode == Camera2.FLASH_AUTO) {
//            mFlashButton.setText("Flash Auto");
//        } else {
//            mFlashButton.setText("Flash Off");
//        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isAE) {
            camera1.setAutoExposure(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
