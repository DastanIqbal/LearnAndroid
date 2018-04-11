#include "native-lib.h"
#include <cstdio>
#include <iostream>
#include <memory>
#include <stdexcept>
#include <string>
#include <array>
#include <dlfcn.h>

void loadLibrary(jobjectArray pJarray);

std::string exec(const char *cmd) {
    LOGI("inside exec");
    std::array<char, 128> buffer;
    std::string result;
    std::shared_ptr<FILE> pipe(popen(cmd, "r"), pclose);
    if (!pipe) {
        LOGI("popen() failed!");
    }
    while (!feof(pipe.get())) {
        LOGI("Inside loop");
        if (fgets(buffer.data(), 128, pipe.get()) != nullptr) {
            result += buffer.data();
            LOGD("Hell ya %s", result.c_str());
        }
        LOGD("Hell yo %s", result.c_str());
    }
    LOGI("exit exec fun");
    return result;
}

JNIEnv *genv;
extern "C" {
void testing() {
    //return 23;
    LOGI("Testing");
}
int testing1() {
    return 23;
}

void testArray(jobjectArray ffmpegCmds) {
    int length = genv->GetArrayLength(ffmpegCmds);
    LOGI("Length %d", length);

    for (int i = 0; i < length; ++i) {
        jstring jstr = (jstring) genv->GetObjectArrayElement(ffmpegCmds, i);
        const char *cpp_string = genv->GetStringUTFChars(jstr, 0);

        std::cout << cpp_string << "\n";
        LOGI("Element %s", cpp_string);

        genv->ReleaseStringUTFChars(jstr, cpp_string);
        genv->DeleteLocalRef(jstr);
    }
}
};

typedef void (*functionTest)(void);

typedef int (*functionTest1)(void);

typedef void (*functionTest2)(jobjectArray);

void testSymb(jobjectArray ffmpegCmds) {
    void *module = dlopen("/sdcard/libNative.so", RTLD_NOW);
    if (module == NULL) {
        fprintf(stderr, "Could not dlopen(\"libbar.so\"): %s\n",
                dlerror());
        LOGI("Could not dlopen(\"libvideokit.so\"):");
    } else {
        LOGI("Library successfully loaded!");
        functionTest testing = (functionTest) dlsym(module, "testing");
        const char *dlsym_error = dlerror();
        if (dlsym_error == NULL) {
            LOGI("Symbol is missing");
        } else {
            LOGI("Library symbol found!");
            (*testing)();
        }

        functionTest1 testing1 = (functionTest1) dlsym(module, "testing1");
        const char *dlsym_error1 = dlerror();
        if (dlsym(module, "testing1") == NULL) {
            LOGI("Symbol1 is missing");
        } else {
            LOGI("Library symbol found!");
            int progress = (*testing1)();
            LOGI("testing %d", progress);
        }

        functionTest2 testing2 = (functionTest2) dlsym(module, "testArray");
        const char *dlsym_error2 = dlerror();
        if (dlsym(module, "testArray") == NULL) {
            LOGI("Symbol1 is missing");
        } else {
            LOGI("Library symbol found!");
            (*testing2)(ffmpegCmds);
        }
    }
    // dlclose(module);
}

JNIEXPORT jstring
JNICALL
Java_com_dastanapps_NativeWrapper_init(JNIEnv *env, jobject object, jobjectArray ffmpegCmds) {
    genv = env;
    LOGI("Hello From the Native Side!!");
    std::string hello = "Hello from C++";
    //std::string cmd = "/data/data/com.dastanapps.processbuilderex/filesffmpeg -y -i /storage/emulated/0/MP4_20170202_183449.mp4 -filter_complex scale=640:640 -strict experimental /data/data/com.dastanapps.processbuilderex/files/outuput.mp4";
    //exec(cmd.c_str());
    int length = env->GetArrayLength(ffmpegCmds);
    LOGI("Length %d", length);

    for (int i = 0; i < length; ++i) {
        jstring jstr = (jstring) env->GetObjectArrayElement(ffmpegCmds, i);
        const char *cpp_string = env->GetStringUTFChars(jstr, 0);

        std::cout << cpp_string << "\n";
        LOGI("Element %s", cpp_string);

        env->ReleaseStringUTFChars(jstr, cpp_string);
        env->DeleteLocalRef(jstr);
    }
    //loadLibrary(ffmpegCmds);
    testSymb(ffmpegCmds);
    return env->NewStringUTF(hello.c_str());
}

void loadLibrary(jobjectArray pJarray) {
    void *lib = dlopen("/data/data/com.dastanapps.processbuilderex/lib/libvideokit.so", RTLD_NOW);
    if (lib == NULL) {
        fprintf(stderr, "Could not dlopen(\"libbar.so\"): %s\n",
                dlerror());
        LOGI("Could not dlopen(\"libvideokit.so\"):");
    } else {
        LOGI("Library successfully loaded!");
        int
        (*rumFFmpeg)(jobjectArray) = (int (*)(jobjectArray)) dlsym(lib,
                                                                   "Java_com_androvid_ffmpeg_NativeWrapper_run");
        const char *dlsym_error = dlerror();
        if (dlsym_error == NULL) {
            LOGI("Symbol is missing");
        } else {
            LOGI("Library symbol found!");
            //rumFFmpeg(pJarray);
            int *cancelAction = (int (*)) dlsym(lib,
                                                "Java_com_androvid_ffmpeg_NativeWrapper_getProgress");
            int progress = (*cancelAction);
            LOGI("getProgress %d", progress);
        }

    }
}