package com.learnopengles.lesson3

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

/**
 * Created by dastan on 05/03/2018.
 * ask2iqbal@gmail.com
 * 05/03/2018 10:09
 */
class MyRenderer : GLSurfaceView.Renderer {
    val mMVPMatrix = FloatArray(16)
    val mProjectionMatrix = FloatArray(16)
    val mViewMatrix = FloatArray(16)
    val mModelMatrix = FloatArray(16)
    val mLightModelMatrix = FloatArray(16)

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
    private val mBytesPerFloat: Byte = 4

    init {
        mCubeFloatBuffer = ByteBuffer.allocateDirect(mCubeVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeFloatBuffer.put(mCubeVertices).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(mColorVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(mColorVertices).position(0)

        mNormalFloatBuffer = ByteBuffer.allocateDirect(normalData.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mNormalFloatBuffer.put(normalData).position(0)
    }

    /** Size of the position data in elements.  */
    private val mPositionDataSize = 3
    private val mColorDataSize: Int = 4
    private val mNormalDataSize = 3

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))
        /// Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L;
        val angleInDegrees = (360.0f / 10000.0f) * time.toInt();

        GLES20.glUseProgram(mProgramHandle)

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "mMVPMatrix")
        GLUtils.checkGlError("getMVPMatrix")
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "mMVMatrix")
        GLUtils.checkGlError("getMVMatrix")
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "mLightPos")
        GLUtils.checkGlError("getLightPos")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLUtils.checkGlError("getPosition")
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "aColor")
        GLUtils.checkGlError("getColor")
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "aNormal")
        GLUtils.checkGlError("getNormal")

        Matrix.setIdentityM(mLightModelMatrix, 0)
        Matrix.translateM(mLightModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0f, 1f, 0f)
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f)

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPositionVertex, 0)
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0)

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
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

    private val mLightPositionVertex = floatArrayOf(0f, 0f, 0f, 1f)
    private fun drawLight() {
        GLES20.glUseProgram(mLightProgramHandle)
        val mLightMVPMatrixHandle = GLES20.glGetUniformLocation(mLightProgramHandle, "mMVPMatrix")
        val mLightPositionHandle = GLES20.glGetAttribLocation(mLightProgramHandle, "aPosition")

        GLES20.glVertexAttrib3f(mLightPositionHandle, mLightPositionVertex[0], mLightPositionVertex[1], mLightPositionVertex[2])
        GLES20.glDisableVertexAttribArray(mLightPositionHandle)

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mLightMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    private val mLightPosInWorldSpace: FloatArray = FloatArray(4)
    private val mLightPosInEyeSpace: FloatArray = FloatArray(4)

    private fun drawCube() {

        //Vertex Data
        mCubeFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        //Color Data
        mColorFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mColorFloatBuffer)
        GLES20.glEnableVertexAttribArray(mColorHandle)

        //Normal Data
        mNormalFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mNormalFloatBuffer)
        GLES20.glEnableVertexAttribArray(mNormalHandle)

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        //MV Data
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        //Light Data
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
    }

    private var mMVPMatrixHandle: Int = -1
    private var mMVMatrixHandle: Int = -1
    private var mLightPosHandle: Int = -1

    private var mPositionHandle: Int = -1
    private var mColorHandle: Int = -1
    private var mNormalHandle: Int = -1

    private var mLightProgramHandle: Int = -1


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255, 19f / 255, 33f / 255, 1f)
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 1f, 0f, 0f, 0f, -5f, 0f, 1f, 0f)
        mProgramHandle = GLUtils.loadProgram(vs, fs)

        mLightProgramHandle = GLUtils.loadProgram(lightVS, lightFS)
    }


    private var mProgramHandle: Int = -1
    val vs = (""
            + "uniform mat4 mMVPMatrix;\n"
            + "uniform mat4 mMVMatrix;\n" // A constant representing the combined model/view matrix.

            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aColor;\n"
            + "attribute vec3 aNormal;\n"

            + "varying vec3 vPosition;\n"
            + "varying vec4 vColor;\n"
            + "varying vec3 vNormal;\n"

            + "void main(){\n" +
            // Transform the vertex into eye space.
            "   vPosition = vec3(mMVMatrix * aPosition);\n" +
            // Transform the normal's orientation into eye space.
            "   vNormal = vec3(mMVMatrix * vec4(aNormal, 0.0));\n" +
            "   vColor = aColor;\n" +
            // gl_Position is a special variable used to store the final position.
            // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
            "   gl_Position = mMVPMatrix * aPosition;\n" +
            "}\n"
            )
    val fs = (""
            + "precision mediump float;\n"
            + "uniform vec3 mLightPos;\n" // The position of the light in eye space.

            + "varying vec3 vPosition;\n"
            + "varying vec4 vColor;\n"
            + "varying vec3 vNormal;\n"

            + "void main(){\n" +
            // Will be used for attenuation.
            " float distance = length(mLightPos - vPosition);\n" +
            // Get a lighting direction vector from the light to the vertex.
            "   vec3 lightVector = normalize(mLightPos - vPosition);\n" +
            // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
            // pointing in the same direction then it will get max illumination.
            "   float diffuse = max(dot(vNormal, lightVector), 0.1);       \n" +
            // Attenuate the light based on distance.
            "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n" +
            // Multiply the color by the illumination level. It will be interpolated across the triangle.
            " gl_FragColor=vColor * diffuse;\n"
            + "}"
            )

    val lightVS = (""
            + "uniform mat4 mMVPMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "void main(){\n"
            + " gl_Position = mMVPMatrix * aPosition;\n"
            + " gl_PointSize = 5.0;\n"
            + "}"
            )
    val lightFS = ("" +
            "precision mediump float;\n"
            + "void main(){\n"
            + " gl_FragColor=vec4(1.0,1.0,1.0,1.0);\n"
            + "}")
}