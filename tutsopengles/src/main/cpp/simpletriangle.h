//
// Created by dastaniqbal on 06/01/2018.
//

#include "common.h"
#include <GLES2/gl2.h>

GLuint loadShader(GLenum shaderType, const char *shaderSource);

GLuint createProgram(const char *vertexSource, const char *fragmentSource);

bool setupGraphics(int w, int h);

void renderFrame();