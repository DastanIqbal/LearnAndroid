//
// Created by dastaniqbal on 08/01/2018.
//

#include "Texture.h"

static const char glVertexShader[] =
                "attribute vec4 vertexPosition;\n"
                "attribute vec2 vertexTextureCord;\n"
                "varying vec2 textureCord;\n"
                "uniform mat4 projection;\n"
                "uniform mat4 modelView;\n"
                "void main()\n"
                "{\n"
                "    gl_Position = projection * modelView * vertexPosition;\n"
                "    textureCord = vertexTextureCord;\n"
                "}\n";
static const char glFragmentShader[] =
                "precision mediump float;\n"
                "uniform sampler2D texture;\n"
                "varying vec2 textureCord;\n"
                "void main()\n"
                "{\n"
                "    gl_FragColor = texture2D(texture, textureCord);\n"
                "}\n";

GLfloat cubeVertices[] = {
        -1.0f, 1.0f, -1.0f, /* Back. */
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
        -1.0f, 1.0f, -1.0f, /* Top. */
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f, /* Bottom. */
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f
};
GLfloat textureCords[] = {
        1.0f, 1.0f, /* Back. */
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
        0.0f, 1.0f, /* Front. */
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f, /* Left. */
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 1.0f, /* Right. */
        1.0f, 0.0f,
        0.0f, 0.0f,
        0.0f, 1.0f,
        0.0f, 1.0f, /* Top. */
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        0.0f, 0.0f, /* Bottom. */
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
};

GLushort indicies[] = {
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
        15, 12, 14,
        //RIGHT
        16, 17, 18,
        16, 19, 18,
        //REAR
        20, 21, 22,
        20, 23, 22
};

GLuint glProgram;
GLuint vertexLocation;
GLint textureCordLocation;
GLint projectionLocation;
GLint modelViewLocation;
GLint samplerLocation;
GLuint textureId;

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
    textureCordLocation = glGetAttribLocation(glProgram, "vertexTextureCord");

    projectionLocation = glGetUniformLocation(glProgram, "projection");
    modelViewLocation = glGetUniformLocation(glProgram, "modelView");
    samplerLocation = glGetUniformLocation(glProgram, "texture");
    /* Setup the perspective. */
    matrixPerspective(projectionMatrix, 45, (float) width / (float) height, 0.1f, 100);
    glEnable(GL_DEPTH_TEST);
    glViewport(0, 0, width, height);
    /* Load the Texture. */
    textureId = loadSimpleTexture();
    if (textureId == 0) {
        return false;
    } else {
        return true;
    }
}

void renderFrame() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    matrixIdentityFunction(modelViewMatrix);

    matrixRotateX(modelViewMatrix, angle);
    matrixRotateY(modelViewMatrix, angle);

    matrixTranslate(modelViewMatrix, 0.0f, 0.0f, -10.0f);

    glUseProgram(glProgram);

    glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, cubeVertices);
    glEnableVertexAttribArray(vertexLocation);

    glVertexAttribPointer(textureCordLocation, 2, GL_FLOAT, GL_FALSE, 0, textureCords);
    glEnableVertexAttribArray(textureCordLocation);

    glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
    glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);
    /* Set the sampler texture unit to 0. */
    glUniform1i(samplerLocation, 0);

    glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, indicies);

    angle += 1;
    if (angle > 360) {
        angle -= 360;
    }
}
