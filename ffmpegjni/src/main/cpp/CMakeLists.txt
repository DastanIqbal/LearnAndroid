cmake_minimum_required(VERSION 3.4.1)

set(prebuilt_DIR ${CMAKE_SOURCE_DIR}/prebuilt)

add_library(avcodec STATIC IMPORTED)
add_library(avdevice STATIC IMPORTED)
add_library(avfilter STATIC IMPORTED)
add_library(avformat STATIC IMPORTED)
add_library(avutil STATIC IMPORTED)
add_library(postproc STATIC IMPORTED)
add_library(swresample STATIC IMPORTED)
add_library(swscale STATIC IMPORTED)

#add_library(fdk-aac STATIC IMPORTED)
#add_library(mp3lame STATIC IMPORTED)
#add_library(shine STATIC IMPORTED)
add_library(x264 STATIC IMPORTED)

#add_library(crypto STATIC IMPORTED)
#add_library(opus STATIC IMPORTED)
#add_library(ogg STATIC IMPORTED)
#add_library(ssl STATIC IMPORTED)
#add_library(vorbis STATIC IMPORTED)
#add_library(vorbisenc STATIC IMPORTED)
#add_library(vorbisfile STATIC IMPORTED)


set_target_properties(
        avcodec PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavcodec.a)

set_target_properties(
        avdevice PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavdevice.a)

set_target_properties(
        avfilter PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavfilter.a)

set_target_properties(
        avformat PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavformat.a)

set_target_properties(
        avutil PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavutil.a)

set_target_properties(
        swresample PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libswresample.a)

set_target_properties(
        swscale PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libswscale.a)
set_target_properties(
        postproc PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libpostproc.a)



#set_target_properties(
#        fdk-aac PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libfdk-aac.a)
#
#set_target_properties(
#        mp3lame PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libmp3lame.a)
#
#set_target_properties(
#        shine PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libshine.a)


set_target_properties(
        x264 PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libx264.a)

#set_target_properties(
#        opus PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libopus.a)

#set_target_properties(
#        ogg PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libogg.a)
#set_target_properties(
#        crypto PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libcrypto.a)
#set_target_properties(
#        ssl PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libssl.a)

#set_target_properties(
#        vorbis PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libvorbis.a)
#
#set_target_properties(
#        vorbisenc PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libvorbisenc.a)
#
#set_target_properties(
#        vorbisfile PROPERTIES IMPORTED_LOCATION
#        ${prebuilt_DIR}/${ANDROID_ABI}/libvorbisfile.a)

set(
        CMAKE_CXX_FLAGS
        "${CMAKE_CXX_FLAGS} -std=gnu++11")

#set(CMAKE_C_FLAGS  "-fPIC")
#
#if (${ANDROID_ABI} STREQUAL "x86_64")
#    include_directories(${ANDROID_SYSROOT}/usr/include/x86_64-linux-android)
#elseif (${ANDROID_ABI} STREQUAL "x86")
#    include_directories(${ANDROID_SYSROOT}/usr/include/i686-linux-android)
#elseif (${ANDROID_ABI} STREQUAL "arm64-v8a")
#    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
#elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
#    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
#endif()

#set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wall -v -Wl,--no-warn-shared-textrel")

include_directories(${CMAKE_SOURCE_DIR})
set(
        HEADER_FILES
        cmdutils.h
        config.h
        ffmpeg.h
        ffmpegJNI.h
)

add_library(ffmpegjni SHARED
        ffmpegJNI.c
        ffmpeg.c
        cmdutils.c
        ffmpeg_opt.c
        ffmpeg_filter.c
        ffmpeg_hw.c
        )

target_include_directories(
        ffmpegjni PUBLIC
        ${prebuilt_DIR}/include)

target_link_libraries(
        ffmpegjni
        android
        jnigraphics
        log
        avdevice
        avfilter
        postproc
        avformat
        avcodec
        dl
        m
        z
        swresample
        swscale
        avutil
        #mp3lame
        #opus
        #shine
        #vorbis
        #vorbisenc
        #vorbisfile
        #ogg
        #ssl
        x264
        #fdk-aac
        #crypto
)