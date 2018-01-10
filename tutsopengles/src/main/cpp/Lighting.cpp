//
// Created by dastaniqbal on 08/01/2018.
//

#include "common.h"
#include "Matrix.h"

static const char glVertexShader[] =
        "attribute vec4 vertexPosition;\n"
                "attribute vec3 vertexColour;\n"
                "attribute vec3 vertexNormal;\n"
                /* [Add a vertex normal attribute.] */
                "attribute vec3 vertexNormal;\n"
                /* [Add a vertex normal attribute.] */
                "varying vec3 fragColour;\n"
                "uniform mat4 projection;\n"
                "uniform mat4 modelView;\n"
                "void main()\n"
                "{\n"
                "    gl_Position = projection * modelView * vertexPosition;\n"
                "   fragColour = vertexColour;\n"
                "}\n";

static const char glFragmentShader[] =
        "precision mediump float;\n"
                "varying vec3 fragColour;\n"
                "void main()\n"
                "{\n"
                "    gl_FragColor = vec4(fragColour, 1.0);\n"
                "}\n";

GLfloat vertices[] = {
        1.0f, 1.0f, -1.0f, /* Back. */
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        0.0f, 0.0f, -2.0f,

        -1.0f, 1.0f, 1.0f, /* Front. */
        1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        0.0f, 0.0f, 2.0f,

        -1.0f, 1.0f, -1.0f, /* Left. */
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -2.0f, 0.0f, 0.0f,

        1.0f, 1.0f, 1.0f, /* Right. */
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        2.0f, 0.0f, 0.0f,

        -1.0f, -1.0f, 1.0f, /* Bottom. */
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        0.0f, -2.0f, 0.0f,

        -1.0f, 1.0f, -1.0f, /* Top. */
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        0.0f, 2.0f, 0.0f
};
GLfloat colors[] = {
        1.0f, 0.0f, 0.0f, /* Back. */
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,

        0.0f, 1.0f, 0.0f, /* Front. */
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,

        0.0f, 0.0f, 1.0f, /* Left. */
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,

        1.0f, 1.0f, 0.0f, /* Right. */
        1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,

        0.0f, 1.0f, 1.0f, /* Bottom. */
        0.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f,

        1.0f, 0.0f, 1.0f, /* Top. */
        1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f
};

GLfloat normals[] = {
        1.0f, 1.0f, -1.0f, /* Back. */
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        -1.0f, 1.0f, 1.0f, /* Front. */
        1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        -1.0f, 1.0f, -1.0f, /* Left. */
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 1.0f, /* Right. */
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 1.0f, /* Bottom. */
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        0.0f, -1.0f, 0.0f,
        -1.0f, 1.0f, -1.0f, /* Top. */
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 0.0f
};

GLushort indicies[] = {
        0, 2, 4, 0, 4, 1, 1, 4, 3, 2, 3, 4,  /* Back. */
        5, 7, 9, 5, 9, 6, 6, 9, 8, 7, 8, 9,  /* Front. */
        10, 12, 14, 10, 14, 11, 11, 14, 13, 12, 13, 14, /* Left. */
        15, 17, 19, 15, 19, 16, 16, 19, 18, 17, 18, 19, /* Right. */
        20, 22, 24, 20, 24, 21, 21, 24, 23, 22, 23, 24, /* Bottom. */
        25, 27, 29, 25, 29, 26, 26, 29, 28, 27, 28, 29  /* Top. */
};

GLuint glProgram;
GLuint vertexLocation;
GLuint vertexColourLocation;
GLuint vertexNormalLocation;

GLint projectionLocation;
GLint modelViewLocation;

float projectionMatrix[16];
float modelViewMatrix[16];
float angle = 0;

GLuint loadSimpleTexture() {
    /* Texture Object Handle. */
    GLuint textureId;
    /* 3 x 3 Image,  R G B A Channels RAW Format. */
    GLubyte pixels[9 * 4] =
            {
                    18, 140, 171, 255, /* Some Colour Bottom Left. */
                    143, 143, 143, 255, /* Some Colour Bottom Middle. */
                    255, 255, 255, 255, /* Some Colour Bottom Right. */
                    255, 255, 0, 255, /* Yellow Middle Left. */
                    0, 255, 255, 255, /* Some Colour Middle. */
                    255, 0, 255, 255, /* Some Colour Middle Right. */
                    255, 0, 0, 255, /* Red Top Left. */
                    0, 255, 0, 255, /* Green Top Middle. */
                    0, 0, 255, 255, /* Blue Top Right. */
            };

    /* Use tightly packed data. */
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    /* Generate a texture object. */
    glGenTextures(1, &textureId);

    /* Activate a texture. */
    glActiveTexture(GL_TEXTURE0);

    /* Bind the texture object. */
    glBindTexture(GL_TEXTURE_2D, textureId);

    /* Load the texture. */
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 3, 3, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    /* Set the filtering mode. */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    return textureId;
}

bool setupGraphics(int width, int height) {
    glProgram = createProgram(glVertexShader, glFragmentShader);
    if (!glProgram) {
        LOGE ("Could not create program");
        return false;
    }
    vertexLocation = glGetAttribLocation(glProgram, "vertexPosition");
    vertexColourLocation = glGetAttribLocation(glProgram, "vertexColour");
  //  vertexNormalLocation = glGetAttribLocation(glProgram, "vertexNormal");

    projectionLocation = glGetUniformLocation(glProgram, "projection");
    modelViewLocation = glGetUniformLocation(glProgram, "modelView");

    /* Setup the perspective. */
    matrixPerspective(projectionMatrix, 45, (float) width / (float) height, 0.1f, 100);
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

    glUseProgram(glProgram);

    glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(vertexLocation);

    glVertexAttribPointer(vertexColourLocation, 3, GL_FLOAT, GL_FALSE, 0, colors);
    glEnableVertexAttribArray(vertexColourLocation);

//    glVertexAttribPointer(vertexNormalLocation, 3, GL_FLOAT, GL_FALSE, 0, normals);
//    glEnableVertexAttribArray(vertexNormalLocation);

    glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
    glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);


    glDrawElements(GL_TRIANGLES, 72, GL_UNSIGNED_SHORT, indicies);

    angle += 1;
    if (angle > 360) {
        angle -= 360;
    }
}
