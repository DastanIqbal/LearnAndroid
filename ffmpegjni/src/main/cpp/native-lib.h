#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>


#define LOG_TAG "JNI::DEBUG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

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