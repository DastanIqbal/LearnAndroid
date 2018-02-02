package com.iaandroid.tutsopengles;

import android.app.Activity;
import android.opengl.GLES20;
import android.os.Bundle;

import com.iaandroid.tutsopengles.gles.GLTextureView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity implements GLTextureView.Renderer {

    private int _program;
    private float _animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLTextureView glSurfaceView = new GLTextureView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setRenderer(this);
        setContentView(glSurfaceView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 0);
        String vertexShaderSource = ""
                + "uniform vec2 translate;"
                + "attribute vec4 position;"
                + "void main(){"
                + "     gl_Position=position+vec4(translate.x,translate.y,0,0);"
                + "}";

        String fragmentShaderSource = ""
                + ""
                + "void main(){"
                + " gl_FragColor=vec4(1,1,0,1.0);"
                + "}";

        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderSource);
        GLES20.glCompileShader(vertexShader);
        String vertexShaderLog = GLES20.glGetProgramInfoLog(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);
        String fragmentShaderLog = GLES20.glGetProgramInfoLog(fragmentShader);

        _program = GLES20.glCreateProgram();
        GLES20.glAttachShader(_program, vertexShader);
        GLES20.glAttachShader(_program, fragmentShader);
        GLES20.glBindAttribLocation(_program, 0, "position");
        GLES20.glLinkProgram(_program);
        String programLog = GLES20.glGetProgramInfoLog(_program);

        GLES20.glUseProgram(_program);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        _animate += 0.01f;
        float translateX = (float) Math.sin(_animate);
        float translateY = 0.1f;

        GLES20.glUniform2f(GLES20.glGetUniformLocation(_program, "translate"), translateX, translateY);

        float gemotry[] = {
                -0.5f, -.5f, 0.0f, 1.0f,
                .5f, -.5f, .0f, 1.0f,
                0.0f, .5f, .0f, 1.0f
        };

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(gemotry.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(gemotry);
        floatBuffer.rewind();//floatBuffer.position(0);

        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 4 * 4, floatBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
