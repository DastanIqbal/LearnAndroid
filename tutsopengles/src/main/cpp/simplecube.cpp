//
// Created by dastaniqbal on 07/01/2018.
//

#include "simplecube.h"

static const char glVertexShader[] =
        "attribute vec4 vertexPosition;\n"
                "attribute vec3 vertexColour;\n"
                "varying vec3 fragColour;\n"
                "uniform mat4 projection;\n"
                "uniform mat4 modelView;\n"
                "void main()\n"
                "{\n"
                "    gl_Position = projection * modelView * vertexPosition;\n"
                "    fragColour = vertexColour;\n"
                "}\n";

static const char glFragmentShader[] =
        "precision mediump float;\n"
                "varying vec3 fragColour;\n"
                "void main()\n"
                "{\n"
                "    gl_FragColor = vec4(fragColour, 1.0);\n"
                "}\n";

GLuint simpleCubeProgram;
GLuint vertexLocation;
GLuint vertexColourLocation;
GLuint projectionLocation;
GLuint modelViewLocation;

float projectionMatrix[16];
float modelViewMatrix[16];
float angle = 0;

GLfloat cubeVertices[] = {-1.0f, 1.0f, -1.0f, /* Back. */
                          1.0f, 1.0f, -1.0f,
                          -1.0f, -1.0f, -1.0f,
                          1.0f, -1.0f, -1.0f,
                          -1.0f, 1.0f, 1.0f, /* Front. */
                          1.0f, 1.0f, 1.0f,
                          -1.0f, -1.0f, 1.0f,
                          1.0f, -1.0f, 1.0f,
                          -1.0f, 1.0f, -1.0f, /* Left. */
                          -1.0f, -1.0f, -1.0f,
                          -1.0f, -1.0f, 1.0f,
                          -1.0f, 1.0f, 1.0f,
                          1.0f, 1.0f, -1.0f, /* Right. */
                          1.0f, -1.0f, -1.0f,
                          1.0f, -1.0f, 1.0f,
                          1.0f, 1.0f, 1.0f,
                          -1.0f, -1.0f, -1.0f, /* Top. */
                          -1.0f, -1.0f, 1.0f,
                          1.0f, -1.0f, 1.0f,
                          1.0f, -1.0f, -1.0f,
                          -1.0f, 1.0f, -1.0f, /* Bottom. */
                          -1.0f, 1.0f, 1.0f,
                          1.0f, 1.0f, 1.0f,
                          1.0f, 1.0f, -1.0f
};

GLfloat colour[] = {1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f
};

GLushort indices[] = {
        //FRONT
        0, 2, 3,
        0, 1, 3,
        //TOP
        4, 6, 7,
        4, 5, 7,
        //BOTTOM
        8, 9, 10,
        11, 8, 10,
        //LEFT
        12, 13, 14,
        15, 12,14,
        //RIGHT
        16, 17, 18,
        16, 19, 18,
        //REAR
        20, 21, 22,
        20, 23, 22
};

bool setupGraphics(int width, int height) {
    simpleCubeProgram = createProgram(glVertexShader, glFragmentShader);
    if (!simpleCubeProgram) {
        LOGE ("Could not create program");
        return false;
    }

    vertexLocation = glGetAttribLocation(simpleCubeProgram, "vertexPosition");
    vertexColourLocation = glGetAttribLocation(simpleCubeProgram, "vertexColour");
    projectionLocation = glGetUniformLocation(simpleCubeProgram, "projection");
    modelViewLocation = glGetUniformLocation(simpleCubeProgram, "modelView");

    matrixPerspective(projectionMatrix, 45, (float) width / (float) height, 0.1f, 100);

    //GL_DEPTH_TEST. This tells OpenGL ES that distance should be a factor in what things are displayed on the screen
    glEnable(GL_DEPTH_TEST);

    glViewport(0, 0, width, height);
    return true;
}

void renderFrame() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    matrixIdentityFunction(modelViewMatrix);

    matrixRotateX(modelViewMatrix, angle);
    matrixRotateY(modelViewMatrix, angle);

    matrixTranslate(modelViewMatrix, 0.0f, 0.0f, -10.0f);

    glUseProgram(simpleCubeProgram);
    glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, cubeVertices);
    glEnableVertexAttribArray(vertexLocation);
    glVertexAttribPointer(vertexColourLocation, 3, GL_FLOAT, GL_FALSE, 0, colour);
    glEnableVertexAttribArray(vertexColourLocation);

    glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
    glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);

    glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, indices);
    angle += 1;
    if (angle > 360) {
        angle -= 360;
    }
}