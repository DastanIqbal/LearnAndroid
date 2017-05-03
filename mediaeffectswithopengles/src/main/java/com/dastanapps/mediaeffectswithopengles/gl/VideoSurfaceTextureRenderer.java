package com.dastanapps.mediaeffectswithopengles.gl;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VideoSurfaceTextureRenderer extends TextureSurfaceRenderer implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = VideoSurfaceTextureRenderer.class.getSimpleName();
    private static final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "varying vec2 vTextureCoord;\n"
                    + "uniform samplerExternalOES sTexture;\n" + "void main() {\n"
                    + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                    + "  float colorR = (color.r + color.g + color.b) / 3.0;\n"
                    + "  float colorG = (color.r + color.g + color.b) / 3.0;\n"
                    + "  float colorB = (color.r + color.g + color.b) / 3.0;\n"
                    + "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n"
                    + "}\n";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f, 1.0f, -1.0f, 0, 1.f, 0.f, -1.0f,
            1.0f, 0, 0.f, 1.f, 1.0f, 1.0f, 0, 1.f, 1.f,};

    private FloatBuffer mTriangleVertices;
    private final String mVertexShader = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID[] = new int[2];
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;


    private Context ctx;
    private SurfaceTexture videoTexture;
    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private boolean frameAvailable = false;

    private int videoWidth;
    private int videoHeight;
    private boolean adjustViewport = false;

    public VideoSurfaceTextureRenderer(Context context, SurfaceTexture texture, int width, int height) {
        super(texture, width, height);
        this.ctx = context;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,
                    compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,
                    linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void setupVertexBuffer() {
        mTriangleVertices = ByteBuffer
                .allocateDirect(
                        mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
    }


    private void setupTexture(Context context) {
        mProgram = createProgram(mVertexShader,
                fragmentShaderCode);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20
                .glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram,
                "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uSTMatrix");
        }

        // int[] textures = new int[1];
        GLES20.glGenTextures(2, mTextureID, 0);
        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID[0]);

        // mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
        checkGlError("glBindTexture mTextureID");

        // GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
        // GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        // GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
        // GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        /*
             * Create the SurfaceTexture that will feed this textureID, and pass
			 * it to the MediaPlayer
			 */
        videoTexture = new SurfaceTexture(mTextureID[0]);
        videoTexture.setOnFrameAvailableListener(this);
    }

    @Override
    protected boolean draw() {
        synchronized (this) {
            if (frameAvailable) {
                videoTexture.updateTexImage();
                videoTexture.getTransformMatrix(mSTMatrix);
                frameAvailable = false;
            } else {
                return false;
            }

        }

       /* if (adjustViewport)
            adjustViewport();*/

        mProgram = createProgram(mVertexShader,
                fragmentShaderCode);

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix,
                0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

        return true;
    }

    private void adjustViewport() {
        float surfaceAspect = height / (float) width;
        float videoAspect = videoHeight / (float) videoWidth;

        if (surfaceAspect > videoAspect) {
            float heightRatio = height / (float) videoHeight;
            int newWidth = (int) (width * heightRatio);
            int xOffset = (newWidth - width) / 2;
            GLES20.glViewport(-xOffset, 0, newWidth, height);
        } else {
            float widthRatio = width / (float) videoWidth;
            int newHeight = (int) (height * widthRatio);
            int yOffset = (newHeight - height) / 2;
            GLES20.glViewport(0, -yOffset, width, newHeight);
        }

        adjustViewport = false;
    }

    @Override
    protected void initGLComponents() {
        setupVertexBuffer();
        setupTexture(ctx);
    }

    @Override
    protected void deinitGLComponents() {
        GLES20.glDeleteTextures(2, mTextureID, 0);
        GLES20.glDeleteProgram(mProgram);
        videoTexture.release();
        videoTexture.setOnFrameAvailableListener(null);
    }

    public void setVideoSize(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;
        adjustViewport = true;
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    public SurfaceTexture getVideoTexture() {
        return videoTexture;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            frameAvailable = true;
        }
    }
}
