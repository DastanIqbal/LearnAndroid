//
// Created by dastaniqbal on 20/09/2018.
//

#include <ffprobeJNI.h>
#include <setjmp.h>
#include <malloc.h>
#include <prebuilt/include/libavutil/log.h>
#include <prebuilt/include/libavcodec/jni.h>
#include "andlogs.h"
#include "ffprobe.h"

FFprobeJNIContext g_ctxt;
ProbeCallback probeCallback;
jmp_buf jmp_exit;

int run_cmd_probe(int argc, char **argv, ProbeCallback callback) {
    int res = 0;
    res = setjmp(jmp_exit);
    if (res) {
        return res;
    }

    res = run_probe(argc, argv, callback);
    return res;
}

void sendProbeJavaMsg(JNIEnv *env, jobject instance,
                      jmethodID func, const char *msg) {
    jstring javaMsg = (*env)->NewStringUTF(env, msg);
    (*env)->CallVoidMethod(env, instance, func, javaMsg);
    (*env)->DeleteLocalRef(env, javaMsg);
}

void ffprobeError(char *c) {
    LOGD("JNI::Benchmark %s", c);
    JNIEnv *env;
    jint res = (*g_ctxt.javaVM)->AttachCurrentThread(g_ctxt.javaVM, &env, NULL);
    if (res != JNI_OK) {
        LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
        return;
    }
    jmethodID methodId = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "probeError",
                                             "(Ljava/lang/String;)V");
    sendProbeJavaMsg(env, g_ctxt.jniHelperObj, methodId, c);

    (*g_ctxt.javaVM)->DetachCurrentThread;
}

void ffmpeg_android_log_callback(void *ptr, int level, const char *fmt, va_list vl) {
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);
    //sanitize((uint8_t *)line);
    if (level <= AV_LOG_ERROR || level <= AV_LOG_FATAL) {
        ffprobeError(line);
    }
    if (level <= AV_LOG_WARNING) {
        LOGE("%s", line);
    } else {
        LOGD("%s", line);
    }
}

void showOutput(char *c) {
    LOGD("JNI::", "Ouput: %s", c);
    JNIEnv *env;
    jint res = (*g_ctxt.javaVM)->AttachCurrentThread(g_ctxt.javaVM, &env, NULL);
    if (res != JNI_OK) {
        LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
        return;
    }
    jmethodID methodId = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "probeOutput",
                                             "(Ljava/lang/String;)V");
    sendProbeJavaMsg(env, g_ctxt.jniHelperObj, methodId, c);

    (*g_ctxt.javaVM)->DetachCurrentThread;
}

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    av_jni_set_java_vm(vm, NULL);
    JNIEnv *env;
    memset(&g_ctxt, 0, sizeof(g_ctxt));
    g_ctxt.javaVM = vm;


    jint res = (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        LOGD("JNI", "JNI version not supported");
        return JNI_ERR;
    }

    jclass clz = (*env)->FindClass(env, "com/dastanapps/ffmpegjni/VideoKit");
    g_ctxt.jniHelperClz = (*env)->NewGlobalRef(env, clz);

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    (*vm)->DetachCurrentThread;
    jint res = (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);
    if (JNI_OK != res) {
        (*env)->DeleteGlobalRef(env, g_ctxt.jniHelperObj);
        (*env)->DeleteGlobalRef(env, g_ctxt.jniHelperClz);
        (*g_ctxt.javaVM)->DestroyJavaVM;
    }
}

JNIEXPORT jint
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_runprobe
        (JNIEnv *env, jobject obj, jobjectArray commands) {
    LOGI("Inside JNI Run");
    //To be in same thread
    g_ctxt.jniHelperObj = (*env)->NewGlobalRef(env, obj);
    av_log_set_callback(ffmpeg_android_log_callback);

    int argc = (*env)->GetArrayLength(env, commands);
    char **argv = (char **) malloc(sizeof(char *) * argc);
    jstring *strr = (jstring *) malloc(sizeof(jstring) * argc);
    int i;

    for (i = 0; i < argc; i++) {
        strr[i] = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env, strr[i], 0);
        LOGI("Cmds: %s", argv[i]);
    }
    jint retcode = 0;
    probeCallback.ouput_callback = showOutput;
    retcode = run_cmd_probe(argc, argv, probeCallback);

    for (i = 0; i < argc; ++i) {
        (*env)->ReleaseStringUTFChars(env, strr[i], argv[i]);
    }
    free(argv);
    free(strr);

    return retcode;
}

JNIEXPORT jint
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_stopFFprobe(JNIEnv *env, jobject obj) {
    stop_ffprobe();
}