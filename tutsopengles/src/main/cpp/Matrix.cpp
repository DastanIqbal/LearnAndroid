//
// Created by dastaniqbal on 07/01/2018.
//

#include "common.h"
#include "Matrix.h"

void matrixIdentityFunction(float *matrix) {
    if (matrix == NULL) {
        return;
    }

    matrix[0] = 1.0f;
    matrix[1] = 0.0f;
    matrix[2] = 0.0f;
    matrix[3] = 0.0f;
    matrix[4] = 0.0f;
    matrix[5] = 1.0f;
    matrix[6] = 0.0f;
    matrix[7] = 0.0f;
    matrix[8] = 0.0f;
    matrix[9] = 0.0f;
    matrix[10] = 1.0f;
    matrix[11] = 0.0f;
    matrix[12] = 0.0f;
    matrix[13] = 0.0f;
    matrix[14] = 0.0f;
    matrix[15] = 1.0f;
}

void matrixTranslate(float *matrix, float x, float y, float z) {
    float temporaryMatrix[16];
    matrixIdentityFunction(temporaryMatrix);
    temporaryMatrix[12] = x;
    temporaryMatrix[13] = y;
    temporaryMatrix[14] = z;
    matrixMultiply(matrix, temporaryMatrix, matrix);
}

void matrixMultiply(float *destination, float *operand1, float *operand2) {
    float theResult[16];
    int row, column = 0;
    int i, j = 0;
    for (i = 0; i < 4; i++) {
        for (j = 0; j < 4; j++) {
            theResult[4 * i + j] =
                    operand1[j] * operand2[4 * i] + operand1[4 + j] * operand2[4 * i + 1] +
                    operand1[8 + j] * operand2[4 * i + 2] + operand1[12 + j] * operand2[4 * i + 3];
        }
    }
    for (int i = 0; i < 16; i++) {
        destination[i] = theResult[i];
    }
}


void matrixScale(float *matrix, float x, float y, float z) {
    float tempMatrix[16];
    matrixIdentityFunction(tempMatrix);
    tempMatrix[0] = x;
    tempMatrix[5] = y;
    tempMatrix[10] = z;
    matrixMultiply(matrix, tempMatrix, matrix);
}

void matrixRotateX(float *matrix, float angle) {
    float tempMatrix[16];
    matrixIdentityFunction(tempMatrix);
    tempMatrix[5] = cos(matrixDegreesToRadians(angle));
    tempMatrix[9] = -sin(matrixDegreesToRadians(angle));
    tempMatrix[6] = sin(matrixDegreesToRadians(angle));
    tempMatrix[10] = cos(matrixDegreesToRadians(angle));
    matrixMultiply(matrix, tempMatrix, matrix);
}

void matrixRotateY(float *matrix, float angle) {
    float tempMatrix[16];
    matrixIdentityFunction(tempMatrix);
    tempMatrix[0] = cos(matrixDegreesToRadians(angle));
    tempMatrix[8] = sin(matrixDegreesToRadians(angle));
    tempMatrix[2] = -sin(matrixDegreesToRadians(angle));
    tempMatrix[10] = cos(matrixDegreesToRadians(angle));
    matrixMultiply(matrix, tempMatrix, matrix);
}

void matrixRotateZ(float *matrix, float angle) {
    float tempMatrix[16];
    matrixIdentityFunction(tempMatrix);
    tempMatrix[0] = cos(matrixDegreesToRadians(angle));
    tempMatrix[4] = -sin(matrixDegreesToRadians(angle));
    tempMatrix[1] = sin(matrixDegreesToRadians(angle));
    tempMatrix[5] = cos(matrixDegreesToRadians(angle));
    matrixMultiply(matrix, tempMatrix, matrix);
}

float matrixDegreesToRadians(float degrees) {
    return M_PI * degrees / 180.0f;
}

void
matrixPerspective(float *matrix, float fieldOfView, float aspectRatio, float zNear, float zFar) {
    float ymax, xmax;
    ymax = zNear * tanf(fieldOfView * M_PI / 360.0);
    xmax = ymax * aspectRatio;
    matrixFrustum(matrix, -xmax, xmax, -ymax, ymax, zNear, zFar);
}

void matrixFrustum(float *matrix, float left, float right, float bottom, float top, float zNear,
                   float zFar) {
    float temp, xDistance, yDistance, zDistance;
    temp = 2.0 * zNear;
    xDistance = right - left;
    yDistance = top - bottom;
    zDistance = zFar - zNear;
    matrixIdentityFunction(matrix);
    matrix[0] = temp / xDistance;
    matrix[5] = temp / yDistance;
    matrix[8] = (right + left) / xDistance;
    matrix[9] = (top + bottom) / yDistance;
    matrix[10] = (-zFar - zNear) / zDistance;
    matrix[11] = -1.0f;
    matrix[14] = (-temp * zFar) / zDistance;
    matrix[15] = 0.0f;
}