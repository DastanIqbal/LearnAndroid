package com.dastanapps.camera2;

import android.util.Size;

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 12:14
 */

public interface ICamera2 {
    void cameraOperned(Size mPreviewSize);
    void cameraError(int error);
    void cameraRecordingStarted();
    void requestVideoPermissions();
    void cameraRecordingStopped();
    void updateFlashMode(int flashMode);
}
