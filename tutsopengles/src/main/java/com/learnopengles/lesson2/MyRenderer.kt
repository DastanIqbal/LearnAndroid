package com.learnopengles.lesson2;

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer : GLSurfaceView.Renderer {
    private var mProgram: Int = -1
    /** This will be used to pass in the transformation matrix. */
    private var mMVPMatrixHandle = -1;

    /** This will be used to pass in the modelview matrix. */
    private var mMVMatrixHandle = -1

    /** This will be used to pass in model position information. */
    private var mPositionHandle: Int = -1

    /** This will be used to pass in model color information. */
    private var mColorHandle: Int = -1

    /** This will be used to pass in model normal information.  */
    private var mNormalHandle: Int = 0

    /** How many bytes per float.  */
    private val mBytesPerFloat = 4

    /** Size of the position data in elements.  */
    private val mPositionDataSize = 3

    /** Size of the color data in elements.  */
    private val mColorDataSize = 4

    /** Size of the normal data in elements.  */
    private val mNormalDataSize = 3

    val vs = ("" +
            "uniform mat4 mMVPMatrix;\n" +
            "uniform mat4 mMVMatrix;\n" +
            "uniform vec3 mLightPos;\n" +

            "attribute vec4 aPosition;\n" +
            "attribute vec4 aColor;\n" +
            "attribute vec4 aNormal;\n" +

            "varying vec4 vColor;\n" +

            "void main(){\n" +
            // Transform the vertex into eye space.
            "   vec3 modelViewMatrix = vec3(mMVPMatrix * aPosition);\n" +
            // Transform the normal's orientation into eye space.
            //"   vec3 modelViewNormal = vec3(mMVMatrix * vec4(aNormal, 0.0));\n" +
            "   vColor = aColor;\n" +
            "   gl_Position = mMVPMatrix*aPosition;\n" +
            "}\n" + ""
            );
    val fs = ("" +
            "precision mediump float;\n" +
            "varying vec4 vColor;\n" +
            "void main(){\n" +
            "       gl_FragColor = vColor;\n" +
            "}\n" +
            "")

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    val mModelMatrix = FloatArray(16);

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    val mViewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    val mProjectionMatrix = FloatArray(16)


    /** Allocate storage for the final combined matrix. This will be passed into the shader program */
    val mMVPMatrix = FloatArray(16)

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    val mLightModelMatrix = FloatArray(16)

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.  */
    private val mLightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    /** Used to hold the current position of the light in world space (after transformation via model matrix).  */
    private val mLightPosInWorldSpace = FloatArray(4)

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)  */
    private val mLightPosInEyeSpace = FloatArray(4)


    // X, Y, Z
    val cubePositionData = floatArrayOf(
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

    //R, G, B, A
    val colorData = floatArrayOf(
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
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

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
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f
    )

    // X, Y, Z
    // The normal is used in light calculations and is a vector which points
    // orthogonal to the plane of the surface. For a cube model, the normals
    // should be orthogonal to the points of each face.
    val normalData = floatArrayOf(
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

    init {
        mCubeFloatBuffer = ByteBuffer.allocateDirect(cubePositionData.size * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeFloatBuffer.put(cubePositionData).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(colorData.size * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(colorData).position(0)

        mNormalFloatBuffer = ByteBuffer.allocateDirect(normalData.size * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mNormalFloatBuffer.put(normalData).position(0)

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))
        /// Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L;
        val angleInDegrees = (360.0f / 10000.0f) * time.toInt();
        GLES20.glUseProgram(mProgram)
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "mMVPMatrix")
        GLUtils.checkGlError("getMVPMatrix")
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "mMVMatrix")
        GLUtils.checkGlError("getMVMatrix")
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        GLUtils.checkGlError("getPosition")
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        GLUtils.checkGlError("getColor")
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal")
        GLUtils.checkGlError("getNormal")

        // Preparing LightModel Matrix
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0)
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f)
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f)
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f)

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f); //Right shift to x
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f) // rotating on X-axis
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f); //Left shift to x
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f) //rotating on Y-axis
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f) // Up shift to Y
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f) //Rotating on Z-axis
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f) // Down shift to Y
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 1.0f) //Rotating on Z-axis
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f) // Up shift to Y
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 1.0f) //Rotating on Z-axis
        drawCube()

        drawLight()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
    }

    private var mPointProgram: Int = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255f, 19f / 255f, 33f / 255f, 1f)

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -0.5f, 0f, 0f, -1.0f, 0f, 1f, 0f)

        val stringList = arrayOf("aPosition", "aColor", "aNormal")
        mProgram = GLUtils.loadProgramAndLink(vs, fs, stringList)

        val pointVS = (
                "uniform mat4 mMVPMatrix;\n"
                        + "attribute vec4 aPosition;\n"
                        + "void main(){\n"
                        + "gl_Position=mMVPMatrix*aPosition;\n"
                        + "gl_PointSize=5.0;\n"
                        + "}"
                )
        val pointColorFS = (
                "precision mediump float;\n"
                        + "void main(){\n"
                        + "gl_FragColor=vec4(1.0f,1.0,1.0,1.0);\n"
                        + "}"
                )
        mPointProgram = GLUtils.loadProgramAndLink(pointVS, pointColorFS, arrayOf("aPosition"))
    }

    private fun drawCube() {
        // Pass in the position information
        mCubeFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLUtils.checkGlError("setVertex")

        // Pass in the color information
        mColorFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mColorFloatBuffer)
        GLES20.glEnableVertexAttribArray(mColorHandle)

//        mNormalFloatBuffer.position(0)
//        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mNormalFloatBuffer)
//        GLES20.glEnableVertexAttribArray(mNormalHandle)

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        // Pass in the modelview matrix
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)
        GLUtils.checkGlError("setMVPMatrix")

        //This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun drawLight() {
        GLES20.glUseProgram(mPointProgram)
        val pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgram, "mMVPMatrix")
        val pointPositionHandle = GLES20.glGetAttribLocation(mPointProgram, "aPosition")

        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2])
        GLES20.glDisableVertexAttribArray(pointPositionHandle)

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        //Draw the point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }
}