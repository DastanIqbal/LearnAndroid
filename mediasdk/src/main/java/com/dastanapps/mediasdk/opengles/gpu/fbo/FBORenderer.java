package com.dastanapps.mediasdk.opengles.gpu.fbo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class FBORenderer {

	GLSurfaceView curView;	
		
	//should be power of 2 (POT)
	int fboWidth = 256*4;
	int fboHeight = 256*4;
	
	int fbo_old;
	int fboId;
	int fboTex; 
	int renderBufferId;
	
	int iProgIdRTT;
	int iPositionRTT;
	int iTexCoordsRTT;
	int iTexLocRTT;
	int iTexIdRTT;
	int iVPMatrix;
	
	public float xAngle = 50;
	public float yAngle = 0;
	
	int iTexId1;
	
	Mesh sphere;
	FloatBuffer vertexBuffer, normalBuffer, texBuffer;
	ShortBuffer indexBuffer;
	
	float[] m_fViewMatrix = new float[16];
	float[] m_fProjMatrix = new float[16];
	float[] m_fVPMatrix = new float[16];
	float[] m_fModel = new float[16];
	float[] m_fMVPMatrix = new float[16];
	
	public FBORenderer(GLSurfaceView view) {
		curView = view;
		
		sphere = new Mesh();
		sphere.Sphere(3, 10);
		vertexBuffer = sphere.getVertexBuffer();
		indexBuffer = sphere.getIndecesBuffer();
		texBuffer = sphere.getTextureBuffer();
		normalBuffer = sphere.getNormalsBuffer();
	}
	
	public void LoadShaders()
	{
		String strVShader = 
			    "attribute vec4 a_position;" +
			    "attribute vec2 a_texCoords;" +
			    "uniform mat4 u_ModelViewMatrix;" +
			    "varying vec2 v_texCoords;" +
				"void main()" +
				"{" +
				    "gl_Position = u_ModelViewMatrix * a_position;" +
				    "v_texCoords = a_texCoords;" +
				"}";
		String strFShaderRTT = 
				"precision mediump float;" +
				"varying vec2 v_texCoords;" +
				"uniform sampler2D u_texId;" +				
				"void main()" +
				"{" +
					"gl_FragColor = texture2D(u_texId, v_texCoords);" +
				"}";
		
		iProgIdRTT = Utils.LoadProgram(strVShader, strFShaderRTT);
		
		
		iTexId1 = Utils.LoadTexture(curView, "pattern.png");
	}
		
	public int InitiateFrameBuffer()
	{
		
		Matrix.setLookAtM(m_fViewMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0);
		
		int[] temp = new int[1];
		//generate fbo id
		GLES20.glGenFramebuffers(1, temp, 0);
		fboId = temp[0];
		//generate texture
		GLES20.glGenTextures(1, temp, 0);
		fboTex = temp[0];
		//generate render buffer
		GLES20.glGenRenderbuffers(1, temp, 0);
		renderBufferId = temp[0];
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
		//
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTex);
		
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, fboWidth, fboHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, fboWidth, fboHeight);
		
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex, 0);				
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			
		return fboId;
	}
	
	public void RenderToTexture()
	{
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
		//Matrix.setLookAtM(m_fViewMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0);
		
		GLES20.glViewport(0, 0, fboWidth, fboHeight);
		float ratio = (float)fboWidth/(float)fboHeight;
		float a = 5f;
		Matrix.orthoM(m_fProjMatrix, 0, -a*ratio, a*ratio, -a*ratio, a*ratio, 1, 10);

		Matrix.multiplyMM(m_fVPMatrix, 0, m_fProjMatrix, 0, m_fViewMatrix, 0);
		
		iPositionRTT = GLES20.glGetAttribLocation(iProgIdRTT, "a_position");
		iTexCoordsRTT = GLES20.glGetAttribLocation(iProgIdRTT, "a_texCoords");
		iTexLocRTT = GLES20.glGetUniformLocation(iProgIdRTT, "u_texId");
		iTexLocRTT = GLES20.glGetUniformLocation(iProgIdRTT, "u_texId");
		iVPMatrix = GLES20.glGetUniformLocation(iProgIdRTT, "u_ModelViewMatrix");
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
		
		
		GLES20.glUseProgram(iProgIdRTT);
		
		vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(iPositionRTT, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(iPositionRTT);
		
		texBuffer.position(0);
		GLES20.glVertexAttribPointer(iTexCoordsRTT, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
		GLES20.glEnableVertexAttribArray(iTexCoordsRTT);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iTexId1);
		GLES20.glUniform1i(iTexLocRTT, 0);
		
		Matrix.setIdentityM(m_fModel, 0);
		xAngle += 1f;
		yAngle += 2f;
		Matrix.rotateM(m_fModel, 0, -xAngle, 0, 1, 0);
		Matrix.rotateM(m_fModel, 0, -yAngle, 1, 0, 0);
		
		Matrix.multiplyMM(m_fMVPMatrix, 0, m_fVPMatrix, 0, m_fModel, 0);
		
		GLES20.glUniformMatrix4fv(iVPMatrix, 1, false, m_fMVPMatrix, 0);
		
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, sphere.m_nIndeces, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		
		//GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);	
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}
	

}