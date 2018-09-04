#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>

#define LOGTAG "DEBUG"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOGTAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOGTAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOGTAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOGTAG, __VA_ARGS__)

extern "C" {
JNIEXPORT jstring JNICALL Java_com_dastanapps_NativeWrapper_init
        (JNIEnv *, jobject, jobjectArray);
}