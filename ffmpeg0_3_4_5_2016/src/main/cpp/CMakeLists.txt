cmake_minimum_required(VERSION 3.4.1)

include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()

include_directories(${CMAKE_SOURCE_DIR})

set(LIB_DIR ${CMAKE_SOURCE_DIR}/lib)

set(
        HEADER_FILES
        cmdutils.h
        config.h
        ffmpeg.h
        ffmpegJNI.h
)

add_library(
        ffmpeg SHARED
        ffmpegJNI.c
        ffmpeg.c
        cmdutils.c
        ffmpeg_opt.c
        ffmpeg_filter.c
)

add_library(avcodec SHARED IMPORTED)
#add_library(avdevice SHARED IMPORTED)
add_library(avfilter SHARED IMPORTED)
add_library(avformat SHARED IMPORTED)
add_library(avutil SHARED IMPORTED)
#add_library(postproc SHARED IMPORTED)
add_library(swresample SHARED IMPORTED)
add_library(swscale SHARED IMPORTED)

#add_library(fdk-aac STATIC IMPORTED)
#add_library(mp3lame STATIC IMPORTED)
#add_library(shine STATIC IMPORTED)
#add_library(x264 STATIC IMPORTED)
#add_library(crypto STATIC IMPORTED)
#add_library(opus STATIC IMPORTED)
#add_library(ogg STATIC IMPORTED)
#add_library(ssl STATIC IMPORTED)
#add_library(vorbis STATIC IMPORTED)
#add_library(vorbisenc STATIC IMPORTED)
#add_library(vorbisfile STATIC IMPORTED)

set_target_properties(
        avcodec PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libavcodec-57.so)

#set_target_properties(
#        avdevice PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libavdevice.so)

set_target_properties(
        avfilter PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libavfilter-6.so)

set_target_properties(
        avformat PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libavformat-57.so)

set_target_properties(
        avutil PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libavutil-55.so)

set_target_properties(
        swresample PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libswresample-2.so)

set_target_properties(
        swscale PROPERTIES IMPORTED_LOCATION
        ${LIB_DIR}/${ANDROID_ABI}/libswscale-4.so)

#set_target_properties(
#        postproc PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libpostproc.so)


#set_target_properties(
#        fdk-aac PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libfdk-aac.a)
#
#set_target_properties(
#        mp3lame PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libmp3lame.a)
#
#set_target_properties(
#        shine PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libshine.a)
#
#
#set_target_properties(
#        x264 PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libx264.a)

#set_target_properties(
#        opus PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libopus.a)

#set_target_properties(
#        ogg PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libogg.a)
#set_target_properties(
#        crypto PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libcrypto.a)
#set_target_properties(
#        ssl PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libssl.a)

#set_target_properties(
#        vorbis PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libvorbis.a)
#
#set_target_properties(
#        vorbisenc PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libvorbisenc.a)
#
#set_target_properties(
#        vorbisfile PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libvorbisfile.a)


target_include_directories(
        ffmpeg PUBLIC
        ${LIB_DIR}/include
)

target_link_libraries(
        ffmpeg
        android
        jnigraphics
        log
        cpufeatures
        avutil
        swscale
        swresample
        avcodec
        #avdevice
        avfilter
        #postproc
        avformat
        dl
        m
        z
        #ass
        #fontconfig
        #freetype
        #fribidi
        #x264
        #mp3lame
        #png
        #expat
)