package com.android

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 12/02/2018.
 * dastanIqbal@marvelmedia.com
 * 12/02/2018 9:50
 */
class MyGLRenderer : GLSurfaceView.Renderer {
    private var mTriangle: Triangle? = null
    private val mMVPMatrix = FloatArray(16)//MVP Matrix
    private val mProjectionMatrix = FloatArray(16) //Map OpenGL coordinate systems to Device Coordinate system
    private val mViewMatrix = FloatArray(16) //CameraView
    private val mRotationMatrix = FloatArray(16) //Rotation Matrix
    override fun onDrawFrame(p0: GL10?) {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 1f, 0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // Create a rotation transformation for the triangle
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.90f * time
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, -1f)
        val scratch = FloatArray(16)
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)

        //Draw Shape
        mTriangle?.draw(scratch)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        mTriangle = Triangle()
    }
}