//
// Created by dastaniqbal on 08/01/2018.
//

#include "common.h"
#include "Matrix.h"

static const char glVertexShader[] =
        "attribute vec4 vertexPosition;\n"
                "attribute vec3 vertexColour;\n"
                /* [Add a vertex normal attribute.] */
                "attribute vec3 vertexNormal;\n"
                /* [Add a vertex normal attribute.] */
                "varying vec3 fragColour;\n"
                "uniform mat4 projection;\n"
                "uniform mat4 modelView;\n"
                "void main()\n"
                "{\n"
                /* [Setup scene vectors.] */
                "    vec3 transformedVertexNormal = normalize((modelView * vec4(vertexNormal, 0.0)).xyz);"
                "    vec3 inverseLightDirection = normalize(vec3(0.0, 1.0, 1.0));\n"
                "    fragColour = vec3(0.0);\n"
                /* [Setup scene vectors.] */
                "\n"
                /* [Calculate the diffuse component.] */
                "    vec3 diffuseLightIntensity = vec3(1.0, 1.0, 1.0);\n"
                "    vec3 vertexDiffuseReflectionConstant = vertexColour;\n"
                "    float normalDotLight = max(0.0, dot(transformedVertexNormal, inverseLightDirection));\n"
                "    fragColour += normalDotLight * vertexDiffuseReflectionConstant * diffuseLightIntensity;\n"
                /* [Calculate the diffuse component.] */
                "\n"
                /* [Calculate the ambient component.] */
                "    vec3 ambientLightIntensity = vec3(0.1, 0.1, 0.1);\n"
                "    vec3 vertexAmbientReflectionConstant = vertexColour;\n"
                "    fragColour += vertexAmbientReflectionConstant * ambientLightIntensity;\n"
                /* [Calculate the ambient component.] */
                "\n"
                /* [Calculate the specular component.] */
                "    vec3 inverseEyeDirection = normalize(vec3(0.0, 0.0, 1.0));\n"
                "    vec3 specularLightIntensity = vec3(1.0, 1.0, 1.0);\n"
                "    vec3 vertexSpecularReflectionConstant = vec3(1.0, 1.0, 1.0);\n"
                "    float shininess = 2.0;\n"
                "    vec3 lightReflectionDirection = reflect(vec3(0) - inverseLightDirection, transformedVertexNormal);\n"
                "    float normalDotReflection = max(0.0, dot(inverseEyeDirection, lightReflectionDirection));\n"
                "    fragColour += pow(normalDotReflection, shininess) * vertexSpecularReflectionConstant * specularLightIntensity;\n"
                /* [Calculate the specular component.] */
                "\n"
                "    /* Make sure the fragment colour is between 0 and 1. */"
                "    clamp(fragColour, 0.0, 1.0);\n"
                "\n"
                "    gl_Position = projection * modelView * vertexPosition;\n"
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

GLushort indices[] = {
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


bool setupGraphics(int width, int height) {
    glProgram = createProgram(glVertexShader, glFragmentShader);
    if (!glProgram) {
        LOGE ("Could not create program");
        return false;
    }
    vertexLocation = glGetAttribLocation(glProgram, "vertexPosition");
    vertexColourLocation = glGetAttribLocation(glProgram, "vertexColour");
    vertexNormalLocation = glGetAttribLocation(glProgram, "vertexNormal");

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

    glVertexAttribPointer(vertexNormalLocation, 3, GL_FLOAT, GL_FALSE, 0, normals);
    glEnableVertexAttribArray(vertexNormalLocation);

    glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
    glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);


    glDrawElements(GL_TRIANGLES, 72, GL_UNSIGNED_SHORT, indices);

    angle += 1;
    if (angle > 360) {
        angle -= 360;
    }
}
