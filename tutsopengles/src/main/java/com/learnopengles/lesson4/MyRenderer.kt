package com.learnopengles.lesson4

import android.content.Context
import android.opengl.*
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

    val mLightModelMatrix = FloatArray(16)
    private val mLightPosInWorldSpace: FloatArray = FloatArray(4)
    private val mLightPosInEyeSpace: FloatArray = FloatArray(4)

    private val mLightPositionVertex = floatArrayOf(0f, 0f, 0f, 1f)

    private var mProgramHandle: Int = -1
    private var mLightProgramHandle: Int = -1

    private var mMVPMatrixHandle: Int = -1
    private var mPositionHandle: Int = -1
    private var mColorHandle: Int = -1
    private var mNormalHandle: Int = -1

    private var mMVMatrixHandle: Int = -1
    private var mLightPosHandle: Int = -1
    private var mTextureDataHandle: Int=-1
    private var mTextureUniformHandle: Int = 0
    private var mTextureCoordinateHandle: Int = 0



    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))

        val time = SystemClock.uptimeMillis() % 10000L;
        val angleInDegrees = (360.0f / 10000.0f) * time.toInt()

        GLES20.glUseProgram(mProgramHandle)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        GLUtils.checkGlError("getMVPMatrix")
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVMatrix")
        GLUtils.checkGlError("getMVMatrix")
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "uLightPos")
        GLUtils.checkGlError("getLightPos")
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "uTexture")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLUtils.checkGlError("getPosition")
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "aColor")
        GLUtils.checkGlError("getPosition")
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "aNormal")
        GLUtils.checkGlError("getPosition")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoordinate")

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        Matrix.setIdentityM(mLightModelMatrix, 0)
        Matrix.translateM(mLightModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0f, 1f, 0f)
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f)

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPositionVertex, 0)
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0)

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 0f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 5f, 0f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -5f, 0f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 5f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -5f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        drawLight()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio: Float = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
    }

    val lightVS = (""
            + "uniform mat4 uMVPMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "void main(){\n"
            + " gl_Position = uMVPMatrix * aPosition;\n"
            + " gl_PointSize = 5.0;\n"
            + "}"
            )
    val lightFS = ("" +
            "precision mediump float;\n"
            + "void main(){\n"
            + " gl_FragColor=vec4(1.0,1.0,1.0,1.0);\n"
            + "}")

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255, 19f / 255, 33f / 255, 1f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 1f, 0f, 0f, 0f, -5f, 0f, 1f, 0f)

        mProgramHandle = GLUtils.loadProgram(GLUtils.readTextFileFromRawResource(context, R.raw.vs_lesson4opengles)!!,
                GLUtils.readTextFileFromRawResource(context, R.raw.fs_lesson4opengles)!!)

        mLightProgramHandle = GLUtils.loadProgram(lightVS, lightFS)

        // Load the texture
        mTextureDataHandle = GLUtils.loadTexture(context, R.drawable.bumpy_bricks_public_domain)
    }

    private fun drawLight() {
        GLES20.glUseProgram(mLightProgramHandle)
        val mLightMVPMatrixHandle = GLES20.glGetUniformLocation(mLightProgramHandle, "uMVPMatrix")
        val mLightPositionHandle = GLES20.glGetAttribLocation(mLightProgramHandle, "aPosition")

        GLES20.glVertexAttrib3f(mLightPositionHandle, mLightPositionVertex[0], mLightPositionVertex[1], mLightPositionVertex[2])
        GLES20.glDisableVertexAttribArray(mLightPositionHandle)

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mLightMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    fun drawCube() {
        mCubeFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLUtils.checkGlError("setPosition")

        mColorFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, mColorFloatBuffer)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLUtils.checkGlError("setColor")

        mNormalFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormalFloatBuffer)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLUtils.checkGlError("setNormal")

        // Pass in the texture coordinate information
        mTextureFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureFloatBuffer)
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)
        GLUtils.checkGlError("setTexture")

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        //MV Data
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLUtils.checkGlError("getUniform")

        //Light Data
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

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

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    val cubeTextureCoordinateData = floatArrayOf(
            // Front face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

            // Right face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

            // Back face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

            // Left face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

            // Top face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)

    val mCubeFloatBuffer: FloatBuffer
    val mColorFloatBuffer: FloatBuffer
    val mNormalFloatBuffer: FloatBuffer
    val mTextureFloatBuffer: FloatBuffer
    val mBytesPerFloat: Byte = 4

    init {
        mCubeFloatBuffer = ByteBuffer.allocateDirect(mCubeVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeFloatBuffer.put(mCubeVertices).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(mColorVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(mColorVertices).position(0)

        mNormalFloatBuffer = ByteBuffer.allocateDirect(mNormalVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mNormalFloatBuffer.put(mNormalVertices).position(0)

        mTextureFloatBuffer = ByteBuffer.allocateDirect(cubeTextureCoordinateData.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureFloatBuffer.position(0)
    }
}