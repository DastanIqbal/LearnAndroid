package com.dastanapps.camera2.CameraController;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.dastanapps.camera2.MyDebug;

/**
 * Created by dastaniqbal on 10/02/2018.
 * dastanIqbal@marvelmedia.com
 * 10/02/2018 5:31
 */

public class CameraUtils {
    final static String TAG = "DEBUG:CameraUtils";

    /**
     * Determine whether we support Camera2 API.
     */
    public static boolean initCamera2Support(Context context) {
        if (MyDebug.LOG)
            Log.d(TAG, "initCamera2Support");
        boolean supports_camera2 = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraControllerManager2 manager2 = new CameraControllerManager2(context);
            supports_camera2 = true;
            if (manager2.getNumberOfCameras() == 0) {
                if (MyDebug.LOG)
                    Log.d(TAG, "Camera2 reports 0 cameras");
                supports_camera2 = false;
            }
            for (int i = 0; i < manager2.getNumberOfCameras() && supports_camera2; i++) {
                if (!manager2.allowCamera2Support(i)) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera " + i + " doesn't have limited or full support for Camera2 API");
                    supports_camera2 = false;
                }
            }
        }
        if (MyDebug.LOG)
            Log.d(TAG, "supports_camera2? " + supports_camera2);
        return supports_camera2;
    }
}
