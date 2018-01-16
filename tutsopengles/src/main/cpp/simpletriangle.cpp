//
// Created by dastaniqbal on 06/01/2018.
//
#include "simpletriangle.h"

static const char glVertexShader[] =
                "attribute vec4 vPosition;\n"
                "void main(){\n"
                "   gl_Position = vPosition;\n"
                "}\n";

static const char glFragmentShader[] =
                "precision mediump float;\n"
                "void main(){\n"
                "   gl_FragColor = vec4(1.0,0.0,0.0,1.0);\n"
                "}\n";
const GLfloat triangleVertices[] = {
        0.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f
};


GLuint simpleTriangleProgram;
GLuint vPosition;

bool setupGraphics(int w, int h) {
    simpleTriangleProgram = createProgram(glVertexShader, glFragmentShader);
    if (!simpleTriangleProgram) {
        LOGE ("Could not create program");
        return false;
    }

    vPosition = glGetAttribLocation(simpleTriangleProgram, "vPosition");
    glViewport(0, 0, w, h);
    return true;
}

void renderFrame() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glUseProgram(simpleTriangleProgram);
    glVertexAttribPointer(vPosition, 2, GL_FLOAT, GL_FALSE, 0, triangleVertices);
    glEnableVertexAttribArray(vPosition);
    glDrawArrays(GL_TRIANGLES, 0, 3);
}
