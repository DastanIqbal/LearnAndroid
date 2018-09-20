//
// Created by dastaniqbal on 18/07/2018.
//

#include <jni.h>

#ifndef LEARNANDROID_FFMPEGJNI_H
#define LEARNANDROID_FFMPEGJNI_H
typedef struct ffmpegJNI_context {
    JavaVM *javaVM;
    jclass jniHelperClz;
    jobject jniHelperObj;
} FFmpegJNIContext;

typedef struct callback {
    void (*progress_callback)(char *);

    void (*benchmark_callback)(char *);
} Callback;

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_avformatinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_avcodecinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_avfilterinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_configurationinfo
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_run
        (JNIEnv *, jobject, jobjectArray);

JNIEXPORT void JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_setDebug
        (JNIEnv *, jobject, jboolean);

JNIEXPORT void JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_stopTranscoding
        (JNIEnv *env, jobject obj, jboolean stop);

JNIEXPORT void JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_showStreams
        (JNIEnv *env, jobject obj, jstring filename);

#endif //LEARNANDROID_FFMPEGJNI_H
