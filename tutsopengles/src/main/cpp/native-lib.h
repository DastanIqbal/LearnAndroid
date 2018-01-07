#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>

//#include "simpletriangle.h"
#include "simplecube.h"
#include "common.h"

extern "C" {
JNIEXPORT jstring JNICALL Java_com_armmali_firstnative_NativeLibrary_init(
        JNIEnv *env,
        jobject /* this */);

JNIEXPORT void JNICALL Java_com_armmali_firstnative_NativeLibrary_init2(
        JNIEnv *env, jobject obj, jint width, jint height);

JNIEXPORT void JNICALL Java_com_armmali_firstnative_NativeLibrary_step(
        JNIEnv *env,
        jobject /* this */);
}