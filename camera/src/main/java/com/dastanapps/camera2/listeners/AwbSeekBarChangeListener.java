package com.dastanapps.camera2.listeners;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.SeekBar;

import com.dastanapps.camera2.view.AwbSeekBar;


/**
 * Created by dastaniqbal on 18/01/2018.

 * 18/01/2018 3:07
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AwbSeekBarChangeListener implements AwbSeekBar.OnAwbSeekBarChangeListener {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private CameraCaptureSession.CaptureCallback mPreviewSessionCallback;

    public AwbSeekBarChangeListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession,
                                    Handler mHandler, CameraCaptureSession.CaptureCallback mPreviewSessionCallback) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
    }

    @Override
    public void doInProgress1() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
        updatePreview();
    }

    @Override
    public void doInProgress2() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress3() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress4() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress5() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress6() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
        updatePreview();
    }

    @Override
    public void doInProgress7() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress8() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void onStopTrackingTouch(int num) {
        switch (num) {
            case 0:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
                break;
            case 10:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
                break;
            case 20:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
                break;
            case 30:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
                break;
            case 40:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
                break;
            case 50:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
                break;
            case 60:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
                break;
            case 70:
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
                break;
        }
        updatePreview();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 更新预览
     */
    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }
}
