precision mediump float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
uniform vec3 uLightPos;       	// The position of the light in eye space.
uniform sampler2D uTexture;    // The input texture.

varying vec3 vPosition;		// Interpolated position for this fragment.
varying vec4 vColor;          	// This is the color from the vertex shader interpolated across the
  								// triangle per fragment.
varying vec3 vNormal;         	// Interpolated normal for this fragment.
varying vec2 vTexCoordinate;   // Interpolated texture coordinate per fragment.

// The entry point for our fragment shader.
void main()
{
	// Will be used for attenuation.
    float distance = length(uLightPos - vPosition);

	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(uLightPos - vPosition);

	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
	// pointing in the same direction then it will get max illumination.
    float diffuse = max(dot(vNormal, lightVector), 0.0);

	// Add attenuation.
    diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));

    // Add ambient lighting
    diffuse = diffuse + 0.3;

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = (vColor * diffuse * texture2D(uTexture, vTexCoordinate));
}

