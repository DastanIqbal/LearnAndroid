package com.dastanapps.mediasdk.opengles.gpu.fbo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Utils {
	
	public static Bitmap GetFromAssets(GLSurfaceView view,String name)
	{
		Bitmap img = null;
		//get asset manager
		AssetManager assetManager = view.getContext().getAssets();
	    InputStream istr;
		try {
			//open image to input stream 
			istr = assetManager.open(name);
			//decode input stream
			img = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;		
	}
	
	static public FloatBuffer CreateVertexArray(float[] coord)
	{
		FloatBuffer fb= ByteBuffer.allocateDirect(coord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(coord).position(0);
		return fb;
	}
	
	public static int LoadTexture(GLSurfaceView view, int imgResID){
		Log.d("Utils", "Loadtexture");
		Bitmap img = null;
		int textures[] = new int[1];
		try {
			img = BitmapFactory.decodeResource(view.getResources(), imgResID);
			GLES20.glGenTextures(1, textures, 0);
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
			Log.d("LoadTexture", "Loaded texture"+":H:"+img.getHeight()+":W:"+img.getWidth());
		} catch (Exception e){
			Log.d("LoadTexture", e.toString()+ ":" + e.getMessage()+":"+e.getLocalizedMessage());
		}
		img.recycle();
		return textures[0];		
	}
	
	static public int LoadTexture(GLSurfaceView view, String name){
		Log.d("Utils", "Loadtexture");		
		int textures[] = new int[1];
		Bitmap img = GetFromAssets(view, name);
		try {			
			GLES20.glGenTextures(1, textures, 0);
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
			Log.d("LoadTexture", "Loaded texture"+":H:"+img.getHeight()+":W:"+img.getWidth());
		} catch (Exception e){
			Log.d("LoadTexture", e.toString()+ ":" + e.getMessage()+":"+e.getLocalizedMessage());
		}
		img.recycle();
		return textures[0];		
	}
	
	public static int LoadShader(String strSource, int iType)
	{
		Log.d("Utils", "LoadShader");
		int[] compiled = new int[1];
		int iShader = GLES20.glCreateShader(iType);
		GLES20.glShaderSource(iShader, strSource);
		GLES20.glCompileShader(iShader);
		GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {			
			Log.d("Load Shader Failed", "Compilation\n"+GLES20.glGetShaderInfoLog(iShader));
			return 0;
		}
		return iShader;
	}
	
	public static int LoadProgram(String strVSource, String strFSource)
	{
		Log.d("Utils", "LoadProgram");
		int iVShader;
		int iFShader;
		int iProgId;
		int[] link = new int[1];
		iVShader = LoadShader(strVSource, GLES20.GL_VERTEX_SHADER);
		if (iVShader == 0)
		{
			Log.d("Load Program", "Vertex Shader Failed");
			return 0;
		}
		iFShader = LoadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
		if(iFShader == 0)
		{
			Log.d("Load Program", "Fragment Shader Failed");
			return 0;
		}
		
		iProgId = GLES20.glCreateProgram();
		
		GLES20.glAttachShader(iProgId, iVShader);
		GLES20.glAttachShader(iProgId, iFShader);
		
		GLES20.glLinkProgram(iProgId);
		
		GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
		if (link[0] <= 0) {
			Log.d("Load Program", "Linking Failed");
			return 0;
		}
		GLES20.glDeleteShader(iVShader);
		GLES20.glDeleteShader(iFShader);
		return iProgId;
	}
	
	public static float rnd(float min, float max) {
		 float fRandNum = (float)Math.random();
		    return min + (max - min) * fRandNum;
	}
	
	public static int LoadProgram(Context ctx , String strVertShader, String strFragShader)
	{
		String strVShader = "";
		String strFShader = "";
		try 
		{
			AssetManager assetManager = ctx.getAssets();
			InputStream is = assetManager.open(strVertShader);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String line = br.readLine();
				while (line!=null)
				{
					strVShader += line + "\n";
					line = br.readLine();
				}
			} catch (IOException e) {
				strVShader = "";
				e.printStackTrace();
			}
			Log.d("VSHADER", strVShader);
			is = assetManager.open(strFragShader);
			br = new BufferedReader(new InputStreamReader(is));
			
			
			try {
				String line = br.readLine();
				while (line!=null)
				{
					strFShader += line;
					line = br.readLine();
				}
			} catch (IOException e) {
				strFShader = "";
				e.printStackTrace();
			}
			Log.d("FSHADER", strFShader);
		} catch (Exception e){
			Log.d("LoadTexture", e.toString()+ ":" + e.getMessage()+":"+e.getLocalizedMessage());
		}
		return LoadProgram(strVShader, strFShader);
	}
}
