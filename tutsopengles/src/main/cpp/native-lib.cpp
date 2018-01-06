#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>

#define LOG_TAG "libNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_armmali_firstnative_NativeLibrary_init(
        JNIEnv *env,
        jobject /* this */) {
    LOGI("Hello From the Native Side!!");
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT void
JNICALL
Java_com_armmali_firstnative_NativeLibrary_init(
        JNIEnv *env,
        jobject obj, jint width, jint height) {
    LOGI("Hello From the Native Side!!");
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_armmali_firstnative_NativeLibrary_step(
        JNIEnv *env,
        jobject /* this */) {
    sleep(5);
    LOGI("New Frame Ready to be Drawn!!!!");
}