//
// Created by dastaniqbal on 18/07/2018.
//

#include <jni.h>

#ifndef LEARNANDROID_FFMPEGJNI_H
#define LEARNANDROID_FFMPEGJNI_H

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegso_MainActivity_avformatinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegso_MainActivity_avcodecinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegso_MainActivity_avfilterinfo
        (JNIEnv *, jobject);

JNIEXPORT jstring JNICALL Java_com_dastanapps_ffmpegso_MainActivity_configurationinfo
        (JNIEnv *, jobject);

#endif //LEARNANDROID_FFMPEGJNI_H
