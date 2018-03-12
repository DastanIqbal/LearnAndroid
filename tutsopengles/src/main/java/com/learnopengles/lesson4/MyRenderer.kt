package com.learnopengles.lesson4

import android.content.Context
import android.opengl.GLES10
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.iaandroid.tutsopengles.R
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastan on 12/03/2018.
 * ask2iqbal@gmail.com
 * 12/03/2018 10:49
 */
class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    val mModelMatrix = FloatArray(16)
    val mViewMatrix = FloatArray(16)
    val mProjectionMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)

    private var mMVPMatrixHandle: Int = -1
    private var mPositionHandle: Int = -1

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))

        val time = SystemClock.uptimeMillis() % 10000L;
        val angleInDegrees = (360.0f / 10000.0f) * time.toInt();
        GLES20.glUseProgram(mProgramHandle)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        GLUtils.checkGlError("getMVPMatrix")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLUtils.checkGlError("getPosition")


        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 0f)
        drawCube()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio: Float = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
    }

    private var mProgramHandle: Int = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255, 19f / 255, 33f / 255, 1f)
        //     GLES20.glCullFace(GLES20.GL_CULL_FACE)
        //     GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 1f, 0f, 0f, 0f, -5f, 0f, 1f, 0f)

        mProgramHandle = GLUtils.loadProgram(GLUtils.readTextFileFromRawResource(context, R.raw.vs_lesson4opengles)!!,
                GLUtils.readTextFileFromRawResource(context, R.raw.fs_lesson4opengles)!!)
    }

    fun drawCube() {
        mCubeFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLUtils.checkGlError("setPosition")

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLUtils.checkGlError("getUniform")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    val mBytesPerFloat: Byte = 4
    // X, Y, Z
    val mCubeVertices = floatArrayOf(
            // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.

            // Front face
            -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f
    )

    val mColorVertices = floatArrayOf(
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            // Back face (blue)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,

            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (magenta)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    )

    // X, Y, Z
    // The normal is used in light calculations and is a vector which points
    // orthogonal to the plane of the surface. For a cube model, the normals
    // should be orthogonal to the points of each face.
    val mNormalVertices = floatArrayOf(
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
    )

    val mCubeFloatBuffer: FloatBuffer
    val mColorFloatBuffer: FloatBuffer
    val mNormalFloatBuffer: FloatBuffer
    //val mTextureFloatBuffer: FloatBuffer

    init {
        mCubeFloatBuffer = ByteBuffer.allocateDirect(mCubeVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeFloatBuffer.put(mCubeVertices).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(mColorVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(mColorVertices).position(0)

        mNormalFloatBuffer = ByteBuffer.allocateDirect(mNormalVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mNormalFloatBuffer.put(mNormalVertices).position(0)

//        mTextureFloatBuffer = ByteBuffer.allocateDirect(mTextureVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
//        mTextureFloatBuffer.position(0)
    }
}