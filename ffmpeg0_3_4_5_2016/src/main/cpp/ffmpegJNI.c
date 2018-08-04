//
// Created by dastaniqbal on 18/07/2018.
//

#include <stdio.h>
#include "ffmpegJNI.h"
#include "ffmpeg.h"
#include "andlogs.h"
#include <cpu-features.h>
//Log

#include <jni.h>
#include <android/log.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libavfilter/avfilter.h>
#include <setjmp.h>

// Android log function wrappers
static const char *kTAG = "ffmpegJNI";
FFmpegJNIContext g_ctxt;
Callback jni_callback;

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


void checkABI() {
#if defined(__arm__)
#if defined(__ARM_ARCH_7A__)
#if defined(__ARM_NEON__)
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a/NEON (hard-float)"
#else
#define ABI "armeabi-v7a/NEON"
#endif
#else
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a (hard-float)"
#else
#define ABI "armeabi-v7a"
#endif
#endif
#else
#define ABI "armeabi"
#endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif
    LOGI(ABI);
}

void (*stop_callback)(int stop);

void sendJavaMsg(JNIEnv *env, jobject instance,
                 jmethodID func, const char *msg) {
    jstring javaMsg = (*env)->NewStringUTF(env, msg);
    (*env)->CallVoidMethod(env, instance, func, javaMsg);
    (*env)->DeleteLocalRef(env, javaMsg);
}

void showProgress(char *c) {
    LOGD("JNI::", "Progress: %s", c);
    JNIEnv *env;
    jint res = (*g_ctxt.javaVM)->AttachCurrentThread(g_ctxt.javaVM, &env, NULL);
    if (res != JNI_OK) {
        LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
        return;
    }
    jmethodID showProgressId = (*env)->GetMethodID(env, g_ctxt.jniHelperClz, "showProgress",
                                                   "(Ljava/lang/String;)V");
    sendJavaMsg(env, g_ctxt.jniHelperObj, showProgressId, c);
}

void showBenchmark(char *c) {
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

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
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
    checkABI();
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

JNIEXPORT jint
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_run
        (JNIEnv *env, jobject obj, jobjectArray commands) {

    //To be in same thread
    g_ctxt.jniHelperObj = (*env)->NewGlobalRef(env, obj);

    int argc = (*env)->GetArrayLength(env, commands);
    char *argv[argc];
    LOGI("Inside JNI Run");
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env, js, 0);
        LOGI("Cmds: %s", argv[i]);
    }

    jni_callback.progress_callback = showProgress;
    jni_callback.benchmark_callback = showBenchmark;

    jint result_code = run_cmd(argc, argv, jni_callback);
    //free(argv);
    return result_code;
}

JNIEXPORT void
JNICALL Java_com_dastanapps_ffmpegjni_VideoKit_stopTranscoding
        (JNIEnv *env, jobject obj, jboolean stop) {
    if (stop == JNI_TRUE) {
        stop_ffmpeg(1);
    } else {
        stop_ffmpeg(0);
    }
}

jstring
Java_com_videoeditor_kruso_videolib_ArmArchHelper_cpuArchFromJNI(JNIEnv *env, jobject obj) {
    // Maximum we need to store here is ARM v7-neon
    // Which is 11 char long, so initializing a character array of length 11
    char arch_info[11] = "";

    // checking if CPU is of ARM family or not
    if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM) {
        strcpy(arch_info, "ARM");

        // checking if CPU is ARM v7 or not
        uint64_t cpuFeatures = android_getCpuFeatures();
        if ((cpuFeatures & ANDROID_CPU_ARM_FEATURE_ARMv7) != 0) {
            strcat(arch_info, " v7");

            // checking if CPU is ARM v7 Neon
            if ((cpuFeatures & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
                strcat(arch_info, "-neon");
            }
        }
    }
    return (*env)->NewStringUTF(env, arch_info);
}