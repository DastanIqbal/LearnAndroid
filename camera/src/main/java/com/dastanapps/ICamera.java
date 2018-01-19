package com.dastanapps;

import android.util.Size;

/**
 * Created by dastaniqbal on 19/01/2018.
 * dastanIqbal@marvelmedia.com
 * 19/01/2018 12:39
 */

public interface ICamera {
    void cameraOperned(Size mPreviewSize);

    void cameraError(int error);

    void cameraRecordingStarted();

    void cameraRecordingStopped();

    void updateFlashMode(int flashMode);
}
