/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iaandroid.tutsopengles.frames;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Demonstrate the Frame Buffer Object OpenGL ES extension.
 * <p>
 * This sample renders a scene into an offscreen frame buffer, and then uses the
 * resulting image as a texture to render an onscreen scene.
 */
public class FrameBufferObjectActivity extends Activity {
  private GLSurfaceView mGLSurfaceView;

  private class Renderer implements GLSurfaceView.Renderer {
    private boolean mContextSupportsFrameBufferObject;
    private int mTargetTexture;
    private int mFramebuffer;
    private int mFramebufferWidth = 256;
    private int mFramebufferHeight = 256;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private Triangle mTriangle;
    private Cube mCube;
    private float mAngle;
    /**
     * Setting this to true will change the behavior of this sample. It will
     * suppress the normally onscreen rendering, and it will cause the
     * rendering that would normally be done to the offscreen FBO be
     * rendered onscreen instead. This can be helpful in debugging the
     * rendering algorithm.
     */
    private static final boolean DEBUG_RENDER_OFFSCREEN_ONSCREEN = false;

    public void onDrawFrame(GL10 gl) {
      checkGLError(gl);
      if (mContextSupportsFrameBufferObject) {
        GL11ExtensionPack gl11ep = (GL11ExtensionPack) gl;
        if (DEBUG_RENDER_OFFSCREEN_ONSCREEN) {
          drawOffscreenImage(gl, mSurfaceWidth, mSurfaceHeight);
        } else {
          gl11ep.glBindFramebufferOES(
              GL11ExtensionPack.GL_FRAMEBUFFER_OES, mFramebuffer);
          drawOffscreenImage(gl, mFramebufferWidth,
              mFramebufferHeight);
          gl11ep.glBindFramebufferOES(
              GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
          drawOnscreen(gl, mSurfaceWidth, mSurfaceHeight);
        }
      } else {
        // Current context doesn't support frame buffer objects.
        // Indicate this by drawing a red background.
        gl.glClearColor(1, 0, 0, 0);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
      }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
      checkGLError(gl);
      mSurfaceWidth = width;
      mSurfaceHeight = height;
      gl.glViewport(0, 0, width, height);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      mContextSupportsFrameBufferObject = checkIfContextSupportsFrameBufferObject(gl);
      if (mContextSupportsFrameBufferObject) {
        mTargetTexture = createTargetTexture(gl, mFramebufferWidth,
            mFramebufferHeight);
        mFramebuffer = createFrameBuffer(gl, mFramebufferWidth,
            mFramebufferHeight, mTargetTexture);

        mCube = new Cube();
        mTriangle = new Triangle();
      }
    }

    private void drawOnscreen(GL10 gl, int width, int height) {
      gl.glViewport(0, 0, width, height);
      float ratio = (float) width / height;
      gl.glMatrixMode(GL10.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);

      gl.glClearColor(0, 0, 1, 0);
      gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
      gl.glBindTexture(GL10.GL_TEXTURE_2D, mTargetTexture);

      gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
          GL10.GL_REPLACE);

      gl.glMatrixMode(GL10.GL_MODELVIEW);
      gl.glLoadIdentity();

      GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

      gl.glActiveTexture(GL10.GL_TEXTURE0);

      long time = SystemClock.uptimeMillis() % 4000L;
      float angle = 0.090f * ((int) time);

      gl.glRotatef(angle, 0, 0, 1.0f);

      mTriangle.draw(gl);

      // Restore default state so the other renderer is not affected.

      gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    private void drawOffscreenImage(GL10 gl, int width, int height) {
      gl.glViewport(0, 0, width, height);
      float ratio = (float) width / height;
      gl.glMatrixMode(GL10.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

      gl.glEnable(GL10.GL_CULL_FACE);
      gl.glEnable(GL10.GL_DEPTH_TEST);

      gl.glClearColor(0, 0.5f, 1, 0);
      gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
      gl.glMatrixMode(GL10.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslatef(0, 0, -3.0f);
      gl.glRotatef(mAngle, 0, 1, 0);
      gl.glRotatef(mAngle * 0.25f, 1, 0, 0);

      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

      mCube.draw(gl);

      gl.glRotatef(mAngle * 2.0f, 0, 1, 1);
      gl.glTranslatef(0.5f, 0.5f, 0.5f);

      mCube.draw(gl);

      mAngle += 1.2f;

      // Restore default state so the other renderer is not affected.

      gl.glDisable(GL10.GL_CULL_FACE);
      gl.glDisable(GL10.GL_DEPTH_TEST);
      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    private int createTargetTexture(GL10 gl, int width, int height) {
      int texture;
      int[] textures = new int[1];
      gl.glGenTextures(1, textures, 0);
      texture = textures[0];
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
      gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height,
          0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
          GL10.GL_NEAREST);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
          GL10.GL_LINEAR);
      gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
          GL10.GL_REPEAT);
      gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
          GL10.GL_REPEAT);
      ;
      return texture;
    }

    private int createFrameBuffer(GL10 gl, int width, int height,
        int targetTextureId) {
      GL11ExtensionPack gl11ep = (GL11ExtensionPack) gl;
      int framebuffer;
      int[] framebuffers = new int[1];
      gl11ep.glGenFramebuffersOES(1, framebuffers, 0);
      framebuffer = framebuffers[0];
      gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
          framebuffer);

      int depthbuffer;
      int[] renderbuffers = new int[1];
      gl11ep.glGenRenderbuffersOES(1, renderbuffers, 0);
      depthbuffer = renderbuffers[0];

      gl11ep.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES,
          depthbuffer);
      gl11ep.glRenderbufferStorageOES(
          GL11ExtensionPack.GL_RENDERBUFFER_OES,
          GL11ExtensionPack.GL_DEPTH_COMPONENT16, width, height);
      gl11ep.glFramebufferRenderbufferOES(
          GL11ExtensionPack.GL_FRAMEBUFFER_OES,
          GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
          GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffer);

      gl11ep.glFramebufferTexture2DOES(
          GL11ExtensionPack.GL_FRAMEBUFFER_OES,
          GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
          GL10.GL_TEXTURE_2D, targetTextureId, 0);
      int status = gl11ep
          .glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
      if (status != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
        throw new RuntimeException("Framebuffer is not complete: "
            + Integer.toHexString(status));
      }
      gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
      return framebuffer;
    }

    private boolean checkIfContextSupportsFrameBufferObject(GL10 gl) {
      return checkIfContextSupportsExtension(gl,
          "GL_OES_framebuffer_object");
    }

    /**
     * This is not the fastest way to check for an extension, but fine if we
     * are only checking for a few extensions each time a context is
     * created.
     * 
     * @param gl
     * @param extension
     * @return true if the extension is present in the current context.
     */
    private boolean checkIfContextSupportsExtension(GL10 gl,
        String extension) {
      String extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " ";
      // The extensions string is padded with spaces between extensions,
      // but not
      // necessarily at the beginning or end. For simplicity, add spaces
      // at the
      // beginning and end of the extensions string and the extension
      // string.
      // This means we can avoid special-case checks for the first or last
      // extension, as well as avoid special-case checks when an extension
      // name
      // is the same as the first part of another extension name.
      return extensions.indexOf(" " + extension + " ") >= 0;
    }
  }

  static void checkGLError(GL gl) {
    int error = ((GL10) gl).glGetError();
    if (error != GL10.GL_NO_ERROR) {
      throw new RuntimeException("GLError 0x"
          + Integer.toHexString(error));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create our surface view and set it as the content of our
    // Activity
    mGLSurfaceView = new GLSurfaceView(this);
    mGLSurfaceView.setRenderer(new Renderer());
    setContentView(mGLSurfaceView);
  }

  @Override
  protected void onResume() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onResume();
    mGLSurfaceView.onResume();
  }

  @Override
  protected void onPause() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onPause();
    mGLSurfaceView.onPause();
  }
}

class Triangle {
  public Triangle() {

    // Buffers to be passed to gl*Pointer() functions
    // must be direct, i.e., they must be placed on the
    // native heap where the garbage collector cannot
    // move them.
    //
    // Buffers with multi-byte datatypes (e.g., short, int, float)
    // must have their byte order set to native order

    ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
    vbb.order(ByteOrder.nativeOrder());
    mFVertexBuffer = vbb.asFloatBuffer();

    ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
    tbb.order(ByteOrder.nativeOrder());
    mTexBuffer = tbb.asFloatBuffer();

    ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
    ibb.order(ByteOrder.nativeOrder());
    mIndexBuffer = ibb.asShortBuffer();

    // A unit-sided equalateral triangle centered on the origin.
    float[] coords = {
        // X, Y, Z
        -0.5f, -0.25f, 0, 0.5f, -0.25f, 0, 0.0f, 0.559016994f, 0 };

    for (int i = 0; i < VERTS; i++) {
      for (int j = 0; j < 3; j++) {
        mFVertexBuffer.put(coords[i * 3 + j] * 2.0f);
      }
    }

    for (int i = 0; i < VERTS; i++) {
      for (int j = 0; j < 2; j++) {
        mTexBuffer.put(coords[i * 3 + j] * 2.0f + 0.5f);
      }
    }

    for (int i = 0; i < VERTS; i++) {
      mIndexBuffer.put((short) i);
    }

    mFVertexBuffer.position(0);
    mTexBuffer.position(0);
    mIndexBuffer.position(0);
  }

  public void draw(GL10 gl) {
    gl.glFrontFace(GL10.GL_CCW);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
    gl.glEnable(GL10.GL_TEXTURE_2D);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
    gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
        GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
  }

  private final static int VERTS = 3;

  private FloatBuffer mFVertexBuffer;
  private FloatBuffer mTexBuffer;
  private ShortBuffer mIndexBuffer;
}

/**
 * A vertex shaded cube.
 */
class Cube {
  public Cube() {
    int one = 0x10000;
    int vertices[] = { -one, -one, -one, one, -one, -one, one, one, -one,
        -one, one, -one, -one, -one, one, one, -one, one, one, one,
        one, -one, one, one, };

    int colors[] = { 0, 0, 0, one, one, 0, 0, one, one, one, 0, one, 0,
        one, 0, one, 0, 0, one, one, one, 0, one, one, one, one, one,
        one, 0, one, one, one, };

    byte indices[] = { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2, 7,
        3, 3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6, 5, 3, 0, 1, 3, 1, 2 };

    // Buffers to be passed to gl*Pointer() functions
    // must be direct, i.e., they must be placed on the
    // native heap where the garbage collector cannot
    // move them.
    //
    // Buffers with multi-byte datatypes (e.g., short, int, float)
    // must have their byte order set to native order

    ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
    vbb.order(ByteOrder.nativeOrder());
    mVertexBuffer = vbb.asIntBuffer();
    mVertexBuffer.put(vertices);
    mVertexBuffer.position(0);

    ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
    cbb.order(ByteOrder.nativeOrder());
    mColorBuffer = cbb.asIntBuffer();
    mColorBuffer.put(colors);
    mColorBuffer.position(0);

    mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
    mIndexBuffer.put(indices);
    mIndexBuffer.position(0);
  }

  public void draw(GL10 gl) {
    gl.glFrontFace(GL10.GL_CW);
    gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
    gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
    gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE,
        mIndexBuffer);
  }

  private IntBuffer mVertexBuffer;
  private IntBuffer mColorBuffer;
  private ByteBuffer mIndexBuffer;
}