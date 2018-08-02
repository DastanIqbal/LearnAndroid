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

JNIEXPORT int JNICALL Java_com_dastanapps_ffmpegjni_MainActivity_run
        (JNIEnv *, jobject, jobjectArray);

#endif //LEARNANDROID_FFMPEGJNI_H
