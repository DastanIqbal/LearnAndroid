package com.dastanapps.camera2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.widget.ArrayAdapter;

import com.dastanapps.camera.R;

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 10:16
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class DialogHelper {
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    public static final int REQUEST_VIDEO_PERMISSIONS = 1;

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

    public static class EffectsDialog extends DialogFragment {

        private ArrayAdapter<String> arrayAdapter;
        private CaptureRequest.Builder mPreviewBuilder;
        private CameraCaptureSession mPreviewSession;
        private String[] effects;

        public static EffectsDialog newInstance() {
            EffectsDialog dialog = new EffectsDialog();
            Bundle args = new Bundle();
            return dialog;
        }

        public EffectsDialog setEffects(CameraCaptureSession mPreviewSession, CaptureRequest.Builder mPreviewBuilder, String[] effects) {
            this.mPreviewBuilder = mPreviewBuilder;
            this.mPreviewSession = mPreviewSession;
            this.effects = effects;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, effects);
            return new AlertDialog.Builder(getActivity())
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, Integer.parseInt(effects[which].split("[|]")[0]));
                            try {
                                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .create();
        }

    }

    public static class SceneDialog extends DialogFragment {

        private ArrayAdapter<String> arrayAdapter;
        private CaptureRequest.Builder mPreviewBuilder;
        private CameraCaptureSession mPreviewSession;
        private String[] scenes;

        public static SceneDialog newInstance() {
            SceneDialog dialog = new SceneDialog();
            Bundle args = new Bundle();
            return dialog;
        }

        public SceneDialog setScenes(CameraCaptureSession mPreviewSession, CaptureRequest.Builder mPreviewBuilder, String[] scenes) {
            this.mPreviewBuilder = mPreviewBuilder;
            this.mPreviewSession = mPreviewSession;
            this.scenes = scenes;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, scenes);
            return new AlertDialog.Builder(getActivity())
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                            mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.parseInt(scenes[which].split("[|]")[0]));
                            try {
                                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .create();
        }

    }
}
