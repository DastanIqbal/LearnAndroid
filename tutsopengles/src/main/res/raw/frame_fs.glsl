precision mediump float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
uniform vec4 vColor;          	// This is the color from the vertex shader interpolated across the
uniform sampler2D uTexture;    // The input texture.

varying vec2 vTexCoordinate;   // Interpolated texture coordinate per fragment.

// The entry point for our fragment shader.
void main()
{
	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = (vColor * texture2D(uTexture, vTexCoordinate));
}

