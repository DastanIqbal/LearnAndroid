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

package com.dastanapps.camera

import android.app.Activity
import android.app.Fragment
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v13.app.FragmentCompat
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar

import com.dastanapps.camera.view.Cam1AutoFitTextureView
import com.dastanapps.camera.view.FocusImageView


class Camera1VideoFragment : Fragment(), View.OnClickListener, ICamera1, FragmentCompat.OnRequestPermissionsResultCallback, SeekBar.OnSeekBarChangeListener {

    /**
     * An [Cam1AutoFitTextureView] for camera preview.
     */
    private var mTextureView: Cam1AutoFitTextureView? = null

    /**
     * Button to record video
     */
    private var mButtonVideo: Button? = null

    var surfaceview: SurfaceView
    private var camera1: Camera1? = null
    private var mFlashButton: Button? = null
    private var seekBar: SeekBar? = null
    private var rb: RadioGroup? = null
    private var isAE = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        return inflater.inflate(R.layout.fragment_camera1_video, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mTextureView = view.findViewById(R.id.texture)
        mButtonVideo = view.findViewById(R.id.video)
        mButtonVideo!!.setOnClickListener(this)

        // set the surface view for face detection
        surfaceview = view.findViewById(R.id.surfaceView)
        surfaceview.setZOrderOnTop(true)
        surfaceview.holder.setFormat(PixelFormat.TRANSPARENT) //for making it not visible on camera preview


        mFlashButton = view.findViewById(R.id.btn_flash)
        mFlashButton!!.setOnClickListener(this)
        view.findViewById<View>(R.id.btn_switch).setOnClickListener(this)
        view.findViewById<View>(R.id.effects).setOnClickListener(this)
        view.findViewById<View>(R.id.scenes).setOnClickListener(this)
        view.findViewById<View>(R.id.whitebalance).setOnClickListener(this)

        val mFocusImage = view.findViewById<FocusImageView>(R.id.img_focus)
        seekBar = view.findViewById(R.id.seekbar)
        seekBar!!.max = 100
        seekBar!!.setOnSeekBarChangeListener(this)

        rb = view.findViewById(R.id.rb)
        rb!!.setOnCheckedChangeListener { group, checkedId ->
            if (group.checkedRadioButtonId == view.findViewById<View>(R.id.ae).id) {
                isAE = true
                seekBar!!.progress = 50
                seekBar!!.visibility = View.VISIBLE
            } else {
                isAE = false
                seekBar!!.visibility = View.GONE
            }
        }

        camera1 = Camera1(activity, mTextureView, this)
        //        camera1.setFaceView(mFaceView);
        camera1!!.setFocusImage(mFocusImage)

    }

    override fun onResume() {
        super.onResume()
        camera1!!.onResume()
    }

    override fun onPause() {
        camera1!!.onPause()
        super.onPause()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.video -> {
                camera1!!.toggleRecording()
            }
            R.id.btn_flash -> camera1!!.toggleFlash().show(childFragmentManager, FRAGMENT_DIALOG)
            R.id.btn_switch -> camera1!!.switchFaces()
            R.id.effects -> camera1!!.showEffectsDialog().show(childFragmentManager, FRAGMENT_DIALOG)
            R.id.scenes -> camera1!!.showScenesDialog().show(childFragmentManager, FRAGMENT_DIALOG)
            R.id.whitebalance -> camera1!!.showWhiteBalance().show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    override fun cameraOperned(mPreviewSize: Size) {
        if (null != mTextureView) {
            //Camera2Helper.configureTransform(getActivity(), mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    override fun cameraError(error: Int) {
        if (error == 0) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            DialogHelper.ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            val activity = activity
            activity?.finish()
        }
    }

    override fun cameraRecordingStarted() {
        activity.runOnUiThread {
            // UI
            mButtonVideo!!.setText(R.string.stop)
        }
    }

    override fun cameraRecordingStopped() {
        mButtonVideo!!.setText(R.string.record)
    }

    override fun updateFlashMode(flashMode: Int) {
        //        if (flashMode == Camera2.FLASH_ON) {
        //            mFlashButton.setText("Flash On");
        //        } else if (flashMode == Camera2.FLASH_AUTO) {
        //            mFlashButton.setText("Flash Auto");
        //        } else {
        //            mFlashButton.setText("Flash Off");
        //        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (isAE) {
            camera1!!.setAutoExposure(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    companion object {

        private val TAG = "Camera2VideoFragment"
        private val FRAGMENT_DIALOG = "dialog"

        fun newInstance(): Camera1VideoFragment {
            return Camera1VideoFragment()
        }
    }
}
