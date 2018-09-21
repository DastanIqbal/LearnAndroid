//
// Created by dastaniqbal on 18/07/2018.
//

#include <jni.h>

typedef struct ffprobe_context {
    JavaVM *javaVM;
    jclass jniHelperClz;
    jobject jniHelperObj;
} FFprobeJNIContext;

typedef struct probe_callback {
    void (*ouput_callback)(char *);
} ProbeCallback;

JNIEXPORT jint JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_runprobe
        (JNIEnv *env, jobject obj, jobjectArray commands);

JNIEXPORT jint JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_stopFFprobe
        (JNIEnv *env, jobject obj);