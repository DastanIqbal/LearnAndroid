uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec4 aColor;
attribute vec3 aNormal;

varying vec4 vColor;
void main() {
    gl_Position= uMVPMatrix * aPosition;
}
