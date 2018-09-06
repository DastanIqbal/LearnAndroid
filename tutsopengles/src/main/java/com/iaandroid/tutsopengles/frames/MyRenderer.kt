package com.iaandroid.tutsopengles.frames

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
import android.opengl.GLES20
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Log
import java.nio.ShortBuffer
import javax.swing.UIManager.put


/**
 * Created by dastan on 12/03/2018.
 * ask2iqbal@gmail.com
 * 12/03/2018 10:49
 */
class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    val mViewMatrix = FloatArray(16)
    val mProjectionMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)

    private var mProgramHandle: Int = -1

    private var mMVPMatrixHandle: Int = -1
    private var mPositionHandle: Int = -1
    private var mColorHandle: Int = -1


    private var mTextureDataHandle: Int = -1
    private var mTextureUniformHandle: Int = 0
    private var mTextureCoordinateHandle: Int = 0


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))

        GLES20.glUseProgram(mProgramHandle)

        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLUtils.checkGlError("getPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, vertexBuffer);

        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "aColor")
        //Set the Color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColorVertices, 0)

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "uTexture")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoordinate")

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        //Pass in the texture coordinate information
        mCubeFloatBuffer.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        //Get Handle to Shape's Transformation Matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        GLUtils.checkGlError("getMVPMatrix")

        //Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        //Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

        //Disable Vertex Array
        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio: Float = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 2f, 7f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255, 19f / 255, 33f / 255, 1f)

        mProgramHandle = GLUtils.loadProgram(GLUtils.readTextFileFromRawResource(context, R.raw.frame_vs)!!,
                GLUtils.readTextFileFromRawResource(context, R.raw.frame_fs)!!)

        // Load the texture
        mTextureDataHandle = GLUtils.loadTexture(context, R.drawable.bumpy_bricks_public_domain)
    }

    fun checkGlError(glOperation: String) {
        val error: Int = GLES20.glGetError()
        while (error != GLES20.GL_NO_ERROR) {
            Log.e("DEBUG", "$glOperation: glError $error")
            throw RuntimeException("$glOperation: glError $error")
        }
    }

    // X, Y
    private val mCubeVertices = floatArrayOf(
            -0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f
    )
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) //Order to draw vertices
    private val mColorVertices = floatArrayOf(
            0.63671875f, 0.76953125f, 0.22265625f, 1.0f
    )

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    val vertexBuffer = floatArrayOf(
            -0.5f, 0.5f,   // top left
            -0.5f, -0.5f,   // bottom left
            0.5f, -0.5f,   // bottom right
            0.5f, 0.5f)


    val mCubeTextureCoordinates: FloatBuffer
    val mColorFloatBuffer: FloatBuffer
    val mTextureFloatBuffer: FloatBuffer
    val drawListBuffer: ShortBuffer
    val mBytesPerFloat: Byte = 4

    init {
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(mCubeVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeTextureCoordinates.put(mCubeVertices).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(mColorVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(mColorVertices).position(0)

        mTextureFloatBuffer = ByteBuffer.allocateDirect(vertexBuffer.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureFloatBuffer.put(vertexBuffer).position(0)

        //Initialize byte buffer for the draw list
        drawListBuffer = ByteBuffer.allocateDirect(vertexBuffer.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        drawListBuffer.put(drawOrder).position(0)

    }
}





/*
public class Sprite
{
//Reference to Activity Context
private final Context mActivityContext;

//Added for Textures
private final FloatBuffer mCubeTextureCoordinates;
private int mTextureUniformHandle;
private int mTextureCoordinateHandle;
private final int mTextureCoordinateDataSize = 2;
private int mTextureDataHandle;

private final String vertexShaderCode =
    "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition * uMVPMatrix;" +
            "v_TexCoordinate = a_TexCoordinate" +
            "}";

private final String fragmentShaderCode =
    "precision mediump float;" +
            "uniform vec4 vColor;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
            "}";

private final int shaderProgram;
private final FloatBuffer vertexBuffer;
private final ShortBuffer drawListBuffer;
private int mPositionHandle;
private int mColorHandle;
private int mMVPMatrixHandle;

// number of coordinates per vertex in this array
static final int COORDS_PER_VERTEX = 2;
static float spriteCoords[] = { -0.5f,  0.5f,   // top left
    -0.5f, -0.5f,   // bottom left
    0.5f, -0.5f,   // bottom right
    0.5f,  0.5f }; //top right

private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; //Order to draw vertices
private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

// Set color with red, green, blue and alpha (opacity) values
float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

public Sprite(final Context activityContext)
{
    mActivityContext = activityContext;

    //Initialize Vertex Byte Buffer for Shape Coordinates / # of coordinate values * 4 bytes per float
    ByteBuffer bb = ByteBuffer.allocateDirect(spriteCoords.length * 4);
    //Use the Device's Native Byte Order
    bb.order(ByteOrder.nativeOrder());
    //Create a floating point buffer from the ByteBuffer
    vertexBuffer = bb.asFloatBuffer();
    //Add the coordinates to the FloatBuffer
    vertexBuffer.put(spriteCoords);
    //Set the Buffer to Read the first coordinate
    vertexBuffer.position(0);

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
// OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
// What's more is that the texture coordinates are the same for every face.
final float[] cubeTextureCoordinateData =
        {
            -0.5f,  0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f,  0.5f
        };

mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

//Initialize byte buffer for the draw list
ByteBuffer dlb = ByteBuffer.allocateDirect(spriteCoords.length * 2);
dlb.order(ByteOrder.nativeOrder());
drawListBuffer = dlb.asShortBuffer();
drawListBuffer.put(drawOrder);
drawListBuffer.position(0);

int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

shaderProgram = GLES20.glCreateProgram();
GLES20.glAttachShader(shaderProgram, vertexShader);
GLES20.glAttachShader(shaderProgram, fragmentShader);

//Texture Code
GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");

GLES20.glLinkProgram(shaderProgram);

//Load the texture
mTextureDataHandle = loadTexture(mActivityContext, R.drawable.ic_launcher);
}

public void draw(float[] mvpMatrix)
{
//Add program to OpenGL ES Environment
GLES20.glUseProgram(shaderProgram);

//Get handle to vertex shader's vPosition member
mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

//Enable a handle to the triangle vertices
GLES20.glEnableVertexAttribArray(mPositionHandle);

//Prepare the triangle coordinate data
GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

//Get Handle to Fragment Shader's vColor member
mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

//Set the Color for drawing the triangle
GLES20.glUniform4fv(mColorHandle, 1, color, 0);

//Set Texture Handles and bind Texture
mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");

//Set the active texture unit to texture unit 0.
GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

//Bind the texture to this unit.
GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

//Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
GLES20.glUniform1i(mTextureUniformHandle, 0);

//Pass in the texture coordinate information
mCubeTextureCoordinates.position(0);
GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

//Get Handle to Shape's Transformation Matrix
mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

//Apply the projection and view transformation
GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

//Draw the triangle
GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

//Disable Vertex Array
GLES20.glDisableVertexAttribArray(mPositionHandle);
}

public static int loadTexture(final Context context, final int resourceId)
{
final int[] textureHandle = new int[1];

GLES20.glGenTextures(1, textureHandle, 0);

if (textureHandle[0] != 0)
{
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;   // No pre-scaling

    // Read in the resource
    final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bitmap.recycle();
}

if (textureHandle[0] == 0)
{
    throw new RuntimeException("Error loading texture.");
}

return textureHandle[0];
}
}
 */

/*
public class MyGLRenderer implements GLSurfaceView.Renderer {

private static final String TAG = "MyGLRenderer";
private Context context;

private Sprite sprite;

// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
private final float[] mMVPMatrix = new float[16];
private final float[] mProjectionMatrix = new float[16];
private final float[] mViewMatrix = new float[16];

public MyGLRenderer(Context ctx) {
    this.context = ctx;
}

@Override
public void onSurfaceCreated(GL10 unused, EGLConfig config) {

    // Set the background frame color
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

   sprite = new Sprite(context);
}


@Override
public void onDrawFrame(GL10 unused) {
    sprite.draw(mMVPMatrix);
}

@Override
public void onSurfaceChanged(GL10 unused, int width, int height) {
    // Adjust the viewport based on geometry changes,
    // such as screen rotation
    GLES20.glViewport(0, 0, width, height);

    float ratio = (float) width / height;

    // this projection matrix is applied to object coordinates
    // in the onDrawFrame() method
    Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);

}

public static int loadShader(int type, String shaderCode){

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    int shader = GLES20.glCreateShader(type);

    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderCode);
    GLES20.glCompileShader(shader);

    return shader;
}

public static void checkGlError(String glOperation) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
        Log.e(TAG, glOperation + ": glError " + error);
        throw new RuntimeException(glOperation + ": glError " + error);
    }
}




//NEW
public static int loadTexture(final Context context, final int resourceId)
{
    final int[] textureHandle = new int[1];

    GLES20.glGenTextures(1, textureHandle, 0);

    if (textureHandle[0] != 0)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling

    // Read in the resource
    final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bitmap.recycle();
}

if (textureHandle[0] == 0)
{
    throw new RuntimeException("Error loading texture.");
}

return textureHandle[0];
}
 */