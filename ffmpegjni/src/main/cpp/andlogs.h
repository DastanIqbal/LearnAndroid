//
// Created by dastaniqbal on 17/07/2018.
//

#ifndef LEARNANDROID_ANDLOGS_H
#define LEARNANDROID_ANDLOGS_H

#include <android/log.h>

#define LOG_TAG "JNI::DEBUG"
#define ALOG_TAG "JNI::AV"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)

#define ALOGE(fmt, ...) __android_log_vprint(ANDROID_LOG_ERROR, ALOG_TAG, fmt, ##__VA_ARGS__)
#define ALOGI(fmt, ...) __android_log_vprint(ANDROID_LOG_INFO, ALOG_TAG, fmt, ##__VA_ARGS__)
#define ALOGD(fmt, ...) __android_log_vprint(ANDROID_LOG_DEBUG, ALOG_TAG, fmt, ##__VA_ARGS__)
#define ALOGW(fmt, ...) __android_log_vprint(ANDROID_LOG_WARN, ALOG_TAG, fmt, ##__VA_ARGS__)
#define ALOGV(fmt, ...) __android_log_vprint(ANDROID_LOG_VERBOSE, ALOG_TAG, fmt, ##__VA_ARGS__)

#endif //LEARNANDROID_ANDLOGS_H
