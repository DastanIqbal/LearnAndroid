uniform mat4 uMVPMatrix;
uniform mat4 mMVMatrix; // A constant representing the combined model/view matrix.

attribute vec4 aPosition;
attribute vec4 aColor;
attribute vec3 aNormal;

varying vec3 vPosition;
varying vec4 vColor;
varying vec3 vNormal;

void main(){

    // Transform the vertex into eye space.
   vPosition = vec3(mMVMatrix * aPosition);

    // Transform the normal's orientation into eye space.
   vNormal = vec3(mMVMatrix * vec4(aNormal, 0.0));
   vColor = aColor;

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
   gl_Position = uMVPMatrix * aPosition;
}