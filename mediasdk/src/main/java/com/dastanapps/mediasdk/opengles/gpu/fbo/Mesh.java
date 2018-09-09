package com.dastanapps.mediasdk.opengles.gpu.fbo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Mesh {
	/*float[] fVertices;
	float[] fTexCoords;
	short[] sIndeces;
	float[] fNormals;*/
	
	FloatBuffer m_verticesBuffer;
	FloatBuffer m_texBuffer;
	FloatBuffer m_cubeTexBuffer = null;
	ShortBuffer m_indecesBuffer;
	FloatBuffer m_normalsBuffer;
	
	public int m_nIndeces;
	
	public FloatBuffer getVertexBuffer()
	{
		return m_verticesBuffer;
	}
	
	public FloatBuffer getTextureBuffer()
	{
		return m_texBuffer;
	}
	public FloatBuffer getNormalsBuffer()
	{
		return m_normalsBuffer;
	}
	public ShortBuffer getIndecesBuffer()
	{
		return m_indecesBuffer;
	}
	
	public FloatBuffer getCubeTextureBuffer()
	{
		return m_cubeTexBuffer;
	}
	
	public int Cube(float scale)
	{
		scale *= 0.5f;
		float[] fVertices = {
				scale, scale, scale,  -scale, scale, scale,  -scale,-scale, scale,   scale,-scale, scale,    // v0-v1-v2-v3 front
                scale, scale, scale,   scale,-scale, scale,   scale,-scale,-scale,   scale, scale,-scale,    // v0-v3-v4-v5 right
                scale, scale, scale,   scale, scale,-scale,  -scale, scale,-scale,  -scale, scale, scale,    // v0-v5-v6-v1 top
               -scale, scale, scale,  -scale, scale,-scale,  -scale,-scale,-scale,  -scale,-scale, scale,    // v1-v6-v7-v2 left
               -scale,-scale,-scale,   scale,-scale,-scale,   scale,-scale, scale,  -scale,-scale, scale,    // v7-v4-v3-v2 bottom
                scale,-scale,-scale,  -scale,-scale,-scale,  -scale, scale,-scale,   scale, scale,-scale,
				};
			
		float[] fTexCoords = {
                1, 1,   0, 1,   0, 0,   1, 0,    // v0-v1-v2-v3 front
                0, 1,   0, 0,   1, 0,   1, 1,    // v0-v3-v4-v5 right
                1, 0,   1, 1,   0, 1,   0, 0,    // v0-v5-v6-v1 top
                1, 1,   0, 1,   0, 0,   1, 0,    // v1-v6-v7-v2 left
                0, 0,   1, 0,   1, 1,   0, 1,    // v7-v4-v3-v2 bottom
                0, 0,   1, 0,   1, 1,   0, 1 
                };
		
		float[] fCubeTextureCoords = {
				1, 1, 1,  -1, 1, 1,  -1,-1, 1,   1,-1, 1,    // v0-v1-v2-v3 front
                1, 1, 1,   1,-1, 1,   1,-1,-1,   1, 1,-1,    // v0-v3-v4-v5 right
                1, 1, 1,   1, 1,-1,  -1, 1,-1,  -1, 1, 1,    // v0-v5-v6-v1 top
               -1, 1, 1,  -1, 1,-1,  -1,-1,-1,  -1,-1, 1,    // v1-v6-v7-v2 left
               -1,-1,-1,   1,-1,-1,   1,-1, 1,  -1,-1, 1,    // v7-v4-v3-v2 bottom
                1,-1,-1,  -1,-1,-1,  -1, 1,-1,   1, 1,-1,
				
		};
		
		short[] sIndeces = {
                0, 1, 2,   0, 2, 3,    // front
                4, 5, 6,   4, 6, 7,    // right
                8, 9,10,   8,10,11,    // top
                12,13,14,  12,14,15,    // left
                16,17,18,  16,18,19,    // bottom
                20,21,22,  20,22,23 
               };
		
		float[] fNormals = {
                0, 0, 1,   0, 0, 1,   0, 0, 1,   0, 0, 1,     // v0-v1-v2-v3 front
                1, 0, 0,   1, 0, 0,   1, 0, 0,   1, 0, 0,     // v0-v3-v4-v5 right
                0, 1, 0,   0, 1, 0,   0, 1, 0,   0, 1, 0,     // v0-v5-v6-v1 top
                -1, 0, 0,  -1, 0, 0,  -1, 0, 0,  -1, 0, 0,     // v1-v6-v7-v2 left
                0,-1, 0,   0,-1, 0,   0,-1, 0,   0,-1, 0,     // v7-v4-v3-v2 bottom
                0, 0,-1,   0, 0,-1,   0, 0,-1,   0, 0,-1 
				};   
		
		m_verticesBuffer = ByteBuffer.allocateDirect(fVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_verticesBuffer.put(fVertices).position(0);
		
		m_texBuffer = ByteBuffer.allocateDirect(fTexCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_texBuffer.put(fTexCoords).position(0);
		
		m_cubeTexBuffer= ByteBuffer.allocateDirect(fCubeTextureCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_cubeTexBuffer.put(fCubeTextureCoords).position(0);
		
		m_indecesBuffer = ByteBuffer.allocateDirect(sIndeces.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
		m_indecesBuffer.put(sIndeces).position(0);
		
		m_normalsBuffer = ByteBuffer.allocateDirect(fNormals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_normalsBuffer.put(fNormals);
		
		m_nIndeces = sIndeces.length;
		return m_nIndeces;
	}
	
	/**
	 * creates a Sphere mesh
	 * @param radius, radius of Sphere
	 * @param HSlices, Horizontal Slices
	 * @param VSlices, Vertical Slices
	 * @return, number of indeces
	 */
	public int Sphere(float radius, int numSlices)
	{
		 int numParallels = numSlices;
         int nVertices = (numParallels + 1) * (numSlices + 1);
		
		m_nIndeces = numParallels * numSlices * 6;
		
		m_verticesBuffer = ByteBuffer.allocateDirect(nVertices*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_normalsBuffer = ByteBuffer.allocateDirect(nVertices*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_texBuffer = ByteBuffer.allocateDirect(nVertices*2*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		m_indecesBuffer = ByteBuffer.allocateDirect(m_nIndeces*2).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		
		 int i;
         int j;
         
         float angleStep = ((2.0f * (float) Math.PI) / numSlices);

         for (i = 0; i < numParallels + 1; i++) {
                 for (j = 0; j < numSlices + 1; j++) {
                         int vertex = (i * (numSlices + 1) + j) * 3;

                         m_verticesBuffer
                                         .put(vertex + 0,
                                                         (float) (radius
                                                                         * Math.sin(angleStep * (float) i) * Math
                                                                         .sin(angleStep * (float) j)));

                         m_verticesBuffer.put(vertex + 1,
                                         (float) (radius * Math.cos(angleStep * (float) i)));
                         m_verticesBuffer
                                         .put(vertex + 2,
                                                         (float) (radius
                                                                         * Math.sin(angleStep * (float) i) * Math
                                                                         .cos(angleStep * (float) j)));

                         m_normalsBuffer.put(vertex + 0, m_verticesBuffer.get(vertex + 0) / radius);
                         m_normalsBuffer.put(vertex + 1, m_verticesBuffer.get(vertex + 1) / radius);
                         m_normalsBuffer.put(vertex + 2, m_verticesBuffer.get(vertex + 2) / radius);

                         int texIndex = (i * (numSlices + 1) + j) * 2;
                         m_texBuffer.put(texIndex + 0, (float) j / (float) numSlices);
                         m_texBuffer.put(texIndex + 1, (1.0f - (float) i)
                                         / (float) (numParallels - 1));
                 }
         }

         int index = 0;
         for (i = 0; i < numParallels; i++) {
                 for (j = 0; j < numSlices; j++) {
                         m_indecesBuffer.put(index++, (short) (i * (numSlices + 1) + j));
                         m_indecesBuffer.put(index++, (short) ((i + 1) * (numSlices + 1) + j));
                         m_indecesBuffer.put(index++,
                                         (short) ((i + 1) * (numSlices + 1) + (j + 1)));

                         m_indecesBuffer.put(index++, (short) (i * (numSlices + 1) + j));
                         m_indecesBuffer.put(index++,
                                         (short) ((i + 1) * (numSlices + 1) + (j + 1)));
                         m_indecesBuffer.put(index++, (short) (i * (numSlices + 1) + (j + 1)));

                 }
         }		
		return m_nIndeces;
	}
}