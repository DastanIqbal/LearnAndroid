precision mediump float;
uniform vec3 mLightPos; // The position of the light in eye space.

varying vec3 vPosition;
varying vec4 vColor;
varying vec3 vNormal;

void main(){
    // Will be used for attenuation.
     float distance = length(mLightPos - vPosition);
    // Get a lighting direction vector from the light to the vertex.
       vec3 lightVector = normalize(mLightPos - vPosition);
    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.
       float diffuse = max(dot(vNormal, lightVector), 0.1);
    // Attenuate the light based on distance.
       diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
    // Multiply the color by the illumination level. It will be interpolated across the triangle.
     gl_FragColor=vColor * diffuse;
}