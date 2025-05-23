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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.dastanapps.camera.R;
import com.dastanapps.camera2.view.AwbSeekBar;
import com.dastanapps.camera2.view.Cam2AutoFitTextureView;
import com.dastanapps.camera2.view.FocusImageView;
import com.dastanapps.encoder.MediaAudioEncoder;
import com.dastanapps.encoder.MediaEncoder;
import com.dastanapps.encoder.MediaMuxerWrapper;
import com.dastanapps.encoder.MediaVideoEncoder;
import com.dastanapps.view.FaceOverlayView;

import java.io.IOException;


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2VideoFragment extends Fragment
        implements View.OnClickListener, ICamera2, FragmentCompat.OnRequestPermissionsResultCallback, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "Camera2VideoFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * An {@link Cam2AutoFitTextureView} for camera preview.
     */
    private Cam2AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    public SurfaceView surfaceview;
    private Camera2 camera2;
    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;
    private Button mFlashButton;
    private SeekBar seekBar;
    private AwbSeekBar awbSeekBar;
    private RadioGroup rb;
    private boolean isAE = true;
    private MediaMuxerWrapper mMuxer;

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

        FocusImageView mFocusImage = view.findViewById(R.id.img_focus);
        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(this);

        awbSeekBar = view.findViewById(R.id.awbSeekbar);
        rb = view.findViewById(R.id.rb);
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.getCheckedRadioButtonId() == view.findViewById(R.id.ae).getId()) {
                    isAE = true;
                    seekBar.setProgress(50);
                    awbSeekBar.setVisibility(View.GONE);
                    seekBar.setVisibility(View.VISIBLE);
                } else if (group.getCheckedRadioButtonId() == view.findViewById(R.id.iso).getId()) {
                    isAE = false;
                    seekBar.setProgress(100);
                    awbSeekBar.setVisibility(View.GONE);
                    seekBar.setVisibility(View.VISIBLE);
                } else {
                    isAE = false;
                    awbSeekBar.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.GONE);
                }
            }
        });

        camera2 = new Camera2(getActivity(), mTextureView, this);
        camera2.setFaceView(mFaceView);
        camera2.setFocusImage(mFocusImage);
        camera2.setAwbView(awbSeekBar);

        view.findViewById(R.id.customFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera2.changeFilter();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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
                //camera2.toggleRecording();
                toggleRecording();
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

    @Override
    public void orientationChanged(int rotation) {
        if (rotation == 270 || rotation == 90)
            mFlashButton.setRotation(90);
        else if (rotation == 0 || rotation == 180)
            mFlashButton.setRotation(0);
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
            camera2.setISO(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    boolean isRecording = false;

    void toggleRecording() {
        if (isRecording) {
            stopRecording();
            isRecording = false;
        } else {
            startRecording();
            isRecording = true;
        }
    }

    /**
     * start resorcing
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    private void startRecording() {
        Log.v(TAG, "startRecording:");
        try {
            mMuxer = new MediaMuxerWrapper(".mp4");    // if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                new MediaVideoEncoder(mMuxer, mMediaEncoderListener, 720,1280);
            }
            if (true) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * request stop recording
     */
    private void stopRecording() {
        Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            Log.v(TAG, "onPrepared:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder)
                camera2.setVideoEncoder((MediaVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            Log.v(TAG, "onStopped:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder)
                camera2.setVideoEncoder(null);
        }
    };
}
