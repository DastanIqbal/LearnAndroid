cmake_minimum_required(VERSION 3.4.1)

set(prebuilt_DIR ${CMAKE_SOURCE_DIR}/lib)

add_library(avcodec SHARED IMPORTED)
#add_library(avdevice STATIC IMPORTED)
add_library(avfilter SHARED IMPORTED)
add_library(avformat SHARED IMPORTED)
add_library(avutil SHARED IMPORTED)
#add_library(postproc STATIC IMPORTED)
add_library(swresample SHARED IMPORTED)
add_library(swscale SHARED IMPORTED)
#add_library(freetype STATIC IMPORTED)
#add_library(fribidi STATIC IMPORTED)
#add_library(x264 STATIC IMPORTED)
#add_library(mp3lame STATIC IMPORTED)
#add_library(ass STATIC IMPORTED)
#add_library(fontconfig STATIC IMPORTED)
#add_library(png STATIC IMPORTED)
#add_library(expat STATIC IMPORTED)


set_target_properties(
        avcodec PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavcodec-57.so)

#set_target_properties(
#    avdevice PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavdevice.a)

set_target_properties(
        avfilter PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavfilter-6.so)

set_target_properties(
        avformat PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavformat-57.so)

set_target_properties(
        avutil PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libavutil-55.so)

#set_target_properties(
#    postproc PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libpostproc.a)

set_target_properties(
        swresample PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libswresample-2.so)

set_target_properties(
        swscale PROPERTIES IMPORTED_LOCATION
        ${prebuilt_DIR}/${ANDROID_ABI}/libswscale-4.so)

#set_target_properties(
#    freetype PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libfreetype.a)
#
#set_target_properties(
#    fribidi PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libfribidi.a)
#
#set_target_properties(
#    x264 PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libx264.a)
#
#set_target_properties(
#    mp3lame PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libmp3lame.a)
#
#set_target_properties(
#    ass PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libass.a)
#
#set_target_properties(
#    fontconfig PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libfontconfig.a)
#
#set_target_properties(
#    png PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libpng16.a)
#
#set_target_properties(
#    expat PROPERTIES IMPORTED_LOCATION
#    ${prebuilt_DIR}/${ANDROID_ABI}/libexpat.a)

set(
        CMAKE_CXX_FLAGS
        "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${CMAKE_SOURCE_DIR})
set(
        HEADER_FILES
        cmdutils.h
        config.h
        ffmpeg.h
        ffmpegJNI.h
)

add_library(ffmpegso SHARED
        ffmpegJNI.c
        ffmpeg.c
        cmdutils.c
        ffmpeg_opt.c
        ffmpeg_filter.c
        )

target_include_directories(
        ffmpegso PUBLIC
        ${prebuilt_DIR}/include
)

target_link_libraries(
        ffmpegso
        android
        jnigraphics
        log
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