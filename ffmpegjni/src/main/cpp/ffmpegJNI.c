//
// Created by dastaniqbal on 18/07/2018.
//

#include <stdio.h>
#include <setjmp.h>
#include "ffmpegJNI.h"
#include "ffmpeg.h"
#include "andlogs.h"

//Log
//#ifdef ANDROID

#include <jni.h>
#include <android/log.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libavfilter/avfilter.h>
#include <prebuilt/include/libavcodec/jni.h>

//#endif
//https://blog.csdn.net/matrix_laboratory/article/details/56677084

// Android log function wrappers
static const char *kTAG = "ffmpegJNI";
FFmpegJNIContext g_ctxt;
Callback jni_callback;

void sendJavaMsg(JNIEnv *env, jobject instance,
                 jmethodID func, const char *msg) {
    jstring javaMsg = (*env)->NewStringUTF(env, msg);
    (*env)->CallVoidMethod(env, instance, func, javaMsg);
    (*env)->DeleteLocalRef(env, javaMsg);
}

void showBenchmark(char *c) {
    LOGD("JNI::Benchmark %s", c);
    JNIEnv *env;
    jint res = (*g_ctxt.javaVM)->AttachCurrentThread(g_ctxt.javaVM, &env, NULL);
    if (res != JNI_OK) {
        LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
        return;
    }
    jmethodID methodId = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "showBenchmark",
                                                   "(Ljava/lang/String;)V");
    sendJavaMsg(env, g_ctxt.jniHelperObj, methodId, c);

    (*g_ctxt.javaVM)->DetachCurrentThread;
}

void showProgress(char *c) {
    LOGD("JNI::", "Progress: %s", c);
    JNIEnv *env;
    jint res = (*g_ctxt.javaVM)->AttachCurrentThread(g_ctxt.javaVM, &env, NULL);
    if (res != JNI_OK) {
        LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
        return;
    }
    jmethodID methodId = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "showProgress",
                                                   "(Ljava/lang/String;)V");
    sendJavaMsg(env, g_ctxt.jniHelperObj, methodId, c);

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
        LOGD(kTAG, "JNI version not supported");
        return JNI_ERR;
    }

    jclass clz = (*env)->FindClass(env, "com/dastanapps/ffmpegjni/VideoKit");
    g_ctxt.jniHelperClz = (*env)->NewGlobalRef(env, clz);
//
//    jmethodID jniHelperCtr = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "<init>", "()V");
//    jobject handler = (*env)->NewObject(env, g_ctxt.jniHelperClz, jniHelperCtr);
//
//    g_ctxt.jniHelperObj = (*env)->NewGlobalRef(env, handler);

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
/**
 * com.dastanapps.ffmpegjni_.MainActivity.avformatinfo()
 * AVFormat Support Information
 */
JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_avformatinfo
        (JNIEnv *env, jobject obj) {

    char info[40000] = {0};

    av_register_all();

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    //Input
    while (if_temp != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    //Output
    while (of_temp != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, of_temp->name);
        of_temp = of_temp->next;
    }
    LOGI("%s", info);
    return (*env)->NewStringUTF(env, info);
}

/**
 * com.dastanapps.ffmpegjni_.VideoKit.avcodecinfo()
 * AVCodec Support Information
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegjni_VideoKit_avcodecinfo(JNIEnv *env, jobject obj) {
    char info[40000] = {0};

    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s[Dec]", info);
        } else {
            sprintf(info, "%s[Enc]", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);


        c_temp = c_temp->next;
    }
    LOGI("%s", info);

    return (*env)->NewStringUTF(env, info);
}

/**
 * com.dastanapps.ffmpegjni_.VideoKit.avfilterinfo()
 * AVFilter SupportInformation
 *
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegjni_VideoKit_avfilterinfo(JNIEnv *env, jobject obj) {
    char info[40000] = {0};
    avfilter_register_all();
    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    int i = 0;
    while (f_temp != NULL) {
        sprintf(info, "%s [%10s] \n", info, f_temp->name);
        f_temp = f_temp->next;
    }

    LOGI("%s", info);
    return (*env)->NewStringUTF(env, info);
}

/**
 * com.dastanapps.ffmpegjni_.VideoKit.urlprotocolinfo()
 * Protocol Support Information
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegjni_VideoKit_configurationinfo(JNIEnv *env, jobject obj) {
    char info[10000] = {0};
    av_register_all();


    sprintf(info, "%s\n", avcodec_configuration());

    LOGI("%s", info);
    return (*env)->NewStringUTF(env, info);
}

jmp_buf jmp_exit;

int run_cmd(int argc, char **argv, Callback callback) {
    int res = 0;
    res = setjmp(jmp_exit);
    if (res) {
        return res;
    }

    res = run(argc, argv, callback);
    return res;
}

JNIEXPORT jint
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_run
        (JNIEnv *env, jobject obj, jobjectArray commands) {
    LOGI("Inside JNI Run");

    //To be in same thread
    g_ctxt.jniHelperObj = (*env)->NewGlobalRef(env, obj);

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
    jni_callback.progress_callback = showProgress;
    jni_callback.benchmark_callback = showBenchmark;

    retcode = run_cmd(argc, argv, jni_callback);

    for (i = 0; i < argc; ++i) {
        (*env)->ReleaseStringUTFChars(env, strr[i], argv[i]);
    }
    free(argv);
    free(strr);

    return retcode;
}

jboolean FFMPEG_ANDROID_DEBUG = 0;

void ffmpeg_android_log_callback(void *ptr, int level, const char *fmt, va_list vl) {
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);
    //sanitize((uint8_t *)line);

    if (FFMPEG_ANDROID_DEBUG) {
        if (level <= AV_LOG_WARNING) {
            LOGE("%s", line);
        } else {
            LOGD("%s", line);
        }
    }

}

JNIEXPORT void
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_setDebug
        (JNIEnv *env, jobject obj, jboolean debug) {
    FFMPEG_ANDROID_DEBUG = debug;
    if (debug) {
        av_log_set_callback(ffmpeg_android_log_callback);
    } else {
        av_log_set_callback(NULL);
    }
}