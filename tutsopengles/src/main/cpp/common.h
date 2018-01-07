//
// Created by dastaniqbal on 06/01/2018.
//

#include <stdio.h>
#include <stdlib.h>

#include <android/log.h>
#include <GLES2/gl2.h>

#define LOG_TAG "JNILIB"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

GLuint loadShader(GLenum shaderType, const char *shaderSource);

GLuint createProgram(const char *vertexSource, const char *fragmentSource);