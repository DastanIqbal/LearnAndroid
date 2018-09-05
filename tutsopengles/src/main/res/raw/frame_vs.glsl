uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec2 aTexCoordinate;   // This will be passed into the fragment shader.

varying vec2 vTexCoordinate;   // This will be passed into the fragment shader.

void main(){

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
   gl_Position =  aPosition * uMVPMatrix;
    // Pass through the texture coordinate.
    vTexCoordinate = aTexCoordinate;

}