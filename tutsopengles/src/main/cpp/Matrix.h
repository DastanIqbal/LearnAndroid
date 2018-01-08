//
// Created by dastaniqbal on 07/01/2018.
//

#ifndef MATRIX_H
#define MATRIX_H

void matrixIdentityFunction(float *matrix);

void matrixTranslate(float *matrix, float x, float y, float z);

void matrixMultiply(float *destination, float *operand1, float *operand2);

void matrixScale(float *matrix, float x, float y, float z);

void matrixRotateX(float *matrix, float angle);

void matrixRotateY(float *matrix, float angle);

void matrixRotateZ(float *matrix, float angle);

float matrixDegreesToRadians(float degrees);

void matrixFrustum(float* matrix, float left, float right, float bottom, float top, float zNear, float zFar);

void matrixPerspective(float* matrix, float fieldOfView, float aspectRatio, float zNear, float zFar);

#endif //MATRIX_H
