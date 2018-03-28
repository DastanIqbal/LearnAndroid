//
// Created by dastaniqbal on 25/03/2018.
//

#include <jni.h>
#include <string.h>

extern "C" {
jbyteArray Java_com_dastanapps_audioprocessing_TypeConversionNative_shortToByte
        (JNIEnv *env, jobject obj, jshortArray input);
}

JNIEXPORT jbyteArray JNICALL
Java_com_dastanapps_audioprocessing_TypeConversionNative_shortToByte
        (JNIEnv *env, jobject obj, jshortArray input) {
    jshort *input_array_elements;
    int input_length;

    jbyte *output_array_elements;
    jbyteArray output;

    input_array_elements = env->GetShortArrayElements(input, 0);
    input_length = env->GetArrayLength(input);

    output = (jbyteArray) (env->NewByteArray(input_length * 2));
    output_array_elements = env->GetByteArrayElements(output, 0);

    memcpy(output_array_elements, input_array_elements, (size_t) (input_length * 2));

    env->ReleaseShortArrayElements(input, input_array_elements, JNI_ABORT);
    env->ReleaseByteArrayElements(output, output_array_elements, 0);
    return output;
}