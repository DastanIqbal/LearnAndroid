//
// Created by dastaniqbal on 18/07/2018.
//

#include <stdio.h>
#include "ffmpegJNI.h"
#include "ffmpeg.h"
#include "andlogs.h"
//Log
#ifdef ANDROID

#include <jni.h>
#include <android/log.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libavfilter/avfilter.h>
#endif

/**
 * com.dastanapps.ffmpegso_.MainActivity.avformatinfo()
 * AVFormat Support Information
 */
JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegso_VideoKit_avformatinfo
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
 * com.dastanapps.ffmpegso_.VideoKit.avcodecinfo()
 * AVCodec Support Information
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegso_VideoKit_avcodecinfo(JNIEnv *env, jobject obj) {
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
 * com.dastanapps.ffmpegso_.VideoKit.avfilterinfo()
 * AVFilter SupportInformation
 *
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegso_VideoKit_avfilterinfo(JNIEnv *env, jobject obj) {
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
 * com.dastanapps.ffmpegso_.VideoKit.urlprotocolinfo()
 * Protocol Support Information
 */
JNIEXPORT jstring JNICALL
Java_com_dastanapps_ffmpegso_VideoKit_configurationinfo(JNIEnv *env, jobject obj) {
    char info[10000] = {0};
    av_register_all();


    sprintf(info, "%s\n", avcodec_configuration());

    LOGI("%s", info);
    return (*env)->NewStringUTF(env, info);
}

JNIEXPORT jint
JNICALL Java_com_dastanapps_ffmpegso_VideoKit_run
        (JNIEnv *env, jobject obj, jobjectArray commands) {
    int argc = (*env)->GetArrayLength(env, commands);
    char *argv[argc];
    LOGI("Inside JNI Run");
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env, js, 0);
        LOGI("Cmds: %s", argv[i]);
    }
    return run(argc, argv);
}