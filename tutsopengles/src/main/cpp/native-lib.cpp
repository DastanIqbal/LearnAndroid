#include "native-lib.h"

JNIEXPORT jstring
JNICALL
Java_com_armmali_firstnative_NativeLibrary_init(
        JNIEnv *env,
        jobject /* this */) {
    //LOGI("Hello From the Native Side!!");
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT void
JNICALL
Java_com_armmali_firstnative_NativeLibrary_init2(
        JNIEnv *env,
        jobject obj, jint width, jint height) {
    setupGraphics(width,height);
    LOGI("Hello From the Native Side!!");
}

JNIEXPORT void
JNICALL
Java_com_armmali_firstnative_NativeLibrary_step(
        JNIEnv *env,
        jobject /* this */) {
    //sleep(5);
    renderFrame();
   LOGI("New Frame Ready to be Drawn!!!!");
}