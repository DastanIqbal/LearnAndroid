package com.learnopengles.lesson1

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.SystemClock
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 14/02/2018.
 * dastanIqbal@marvelmedia.com
 * 14/02/2018 11:03
 */
class Lesson1Activity : Activity() {
    lateinit var glSurfaceView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
        setContentView(glSurfaceView)

        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(Lesson1Renderer())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    inner class Lesson1Renderer : GLSurfaceView.Renderer {
        /** Size of the position data in elements. */
        private val mPositionDataSize = 3
        /** How many bytes per float.  */
        private val mBytesPerFloat = 4
        /** How many elements per vertex.  */
        private val mStrideBytes = 7 * mBytesPerFloat

        /**
         * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
         * of being located at the center of the universe) to world space.
         */
        val mModelMatrix = FloatArray(16)
        /**
         * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
         * it positions things relative to our eye.
         */
        val mViewMatrix = FloatArray(16)
        /** Store the projection matrix. This is used to project the scene onto a 2D viewport*/
        val mProjectionMatrix = FloatArray(16)
        /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
        val mMVPMatrix = FloatArray(16)

        val vs = ("uniform mat4 uMVPMatrix;\n" +
                "attribute vec4 vPosition;\n" +
                "attribute vec4 aColor;\n" +
                "varying vec4 vColor;\n" +
                "void main(){\n" +
                "   vColor=aColor;\n" +
                "   gl_Position=uMVPMatrix*vPosition;\n" +
                "}\n")

        val fs = ("precision mediump float;\n" +
                "varying vec4 vColor;\n" +
                "void main(){\n" +
                " gl_FragColor=vColor;\n" +
                "}\n"
                )
        val triangle1 = floatArrayOf(
                //X, Y, Z
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        )
        val triangle2 = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        )
        val triangle3 = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        )

        var tri1FloatBuffer: FloatBuffer
        var tri2FloatBuffer: FloatBuffer
        var tri3FloatBuffer: FloatBuffer

        private var mProgram: Int = -1
        private var mPositionHandler: Int = -1
        private var mMVPMatrixHandler: Int = -1
        private var mColorHandler: Int = -1

        /** Offset of the position data. */
        private var mPositionOffset: Int = 0
        /**Offset of the color data. */
        private val mColorOffset = 3
        /** Size of the color data in elements. */
        private val mColorDataSize = 4

        init {
            var byteBuffer = ByteBuffer.allocateDirect(triangle1.size * 4).order(ByteOrder.nativeOrder())

            tri1FloatBuffer = byteBuffer.asFloatBuffer()
            tri1FloatBuffer.put(triangle1)
            tri1FloatBuffer.position(0)

            byteBuffer = ByteBuffer.allocateDirect(triangle2.size * 4).order(ByteOrder.nativeOrder())

            tri2FloatBuffer = byteBuffer.asFloatBuffer()
            tri2FloatBuffer.put(triangle2)
            tri2FloatBuffer.position(0)

            byteBuffer = ByteBuffer.allocateDirect(triangle3.size * 4).order(ByteOrder.nativeOrder())

            tri3FloatBuffer = byteBuffer.asFloatBuffer()
            tri3FloatBuffer.put(triangle3)
            tri3FloatBuffer.position(0)
        }

        override fun onDrawFrame(p0: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))
            // Do a complete rotation every 10 seconds.
            val time = SystemClock.uptimeMillis() % 10000L;
            val angleInDegrees = ((360.0f / 10000.0f) * (time.toInt()));

            /** Triangle 1 **/
            // Draw the triangle facing straight on.
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
            drawTriangle(tri1FloatBuffer)


            /** Triangle 2 **/
            // Draw one translated a bit down and rotated to be flat on the ground.
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
            drawTriangle(tri2FloatBuffer)

            /** Triangle 3 **/
            // Draw one translated a bit to the right and rotated to be facing to the left.
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
            drawTriangle(tri3FloatBuffer)
        }

        private fun drawTriangle(triBuffer: FloatBuffer) {

            triBuffer.position(mPositionOffset)
            GLES20.glUseProgram(mProgram)
            GLES20.glEnableVertexAttribArray(mPositionHandler)
            GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, triBuffer)
            GLUtils.checkGlError("Vertex Loading")

            triBuffer.position(mColorOffset)
            GLES20.glVertexAttribPointer(mColorHandler, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, triBuffer)
            GLES20.glEnableVertexAttribArray(mColorHandler)
            GLUtils.checkGlError("Color Loading")

            // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
            // (which currently contains model * view).
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
            GLUtils.checkGlError("ModelView Matrix Loading")

            //This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
            // (which now contains model * view * projection).
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

            GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0)
            GLUtils.checkGlError("MVPMatrix Loading")
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

            GLUtils.checkGlError("Draw Triangles")
            GLES20.glDisableVertexAttribArray(mPositionHandler)
        }

        override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
            //Set the OpenGL viewport to the same size as the surface.
            GLES20.glViewport(0, 0, width, height);

            // Create a new perspective projection matrix. The height will stay the same
            // while the width will vary as per aspect ratio.
            val ratio = width.toFloat() / height.toFloat();
            val left = -ratio;
            val right = ratio;
            val bottom = -1.0f;
            val top = 1.0f;
            val near = 1.0f;
            val far = 10.0f;

            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }

        override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(125f / 255f, 253f / 255f, 254f / 255f, 1f)
            // Position the eye behind the origin.
            val eyeX = 0.0f;
            val eyeY = 0.0f;
            val eyeZ = 1.5f;

            // We are looking toward the distance
            val lookX = 0.0f;
            val lookY = 0.0f;
            val lookZ = -5.0f;

            // Set our up vector. This is where our head would be pointing were we holding the camera.
            val upX = 0.0f;
            val upY = 1.0f;
            val upZ = 0.0f;

            // Set the view matrix. This matrix can be said to represent the camera position.
            // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
            // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

            mProgram = GLUtils.loadProgram(vs, fs)
            mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
            GLUtils.checkGlError("Position")
            mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            GLUtils.checkGlError("MVPMatrix")
            mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor")
            GLUtils.checkGlError("Color")
        }

    }
}