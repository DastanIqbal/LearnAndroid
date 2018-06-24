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
add_library(freetype STATIC IMPORTED)
add_library(fribidi STATIC IMPORTED)
add_library(x264 STATIC IMPORTED)
add_library(mp3lame STATIC IMPORTED)
add_library(ass STATIC IMPORTED)
add_library(fontconfig STATIC IMPORTED)
add_library(png STATIC IMPORTED)
add_library(expat STATIC IMPORTED)


set_target_properties(
    avcodec PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavcodec.a)

set_target_properties(
    avdevice PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavdevice.a)

set_target_properties(
    avfilter PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavfilter.a)

set_target_properties(
    avformat PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavformat.a)

set_target_properties(
    avutil PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libavutil.a)

set_target_properties(
    postproc PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libpostproc.a)

set_target_properties(
    swresample PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libswresample.a)

set_target_properties(
    swscale PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libswscale.a)

set_target_properties(
    freetype PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libfreetype.a)

set_target_properties(
    fribidi PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libfribidi.a)

set_target_properties(
    x264 PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libx264.a)

set_target_properties(
    mp3lame PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libmp3lame.a)

set_target_properties(
    ass PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libass.a)

set_target_properties(
    fontconfig PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libfontconfig.a)

set_target_properties(
    png PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libpng16.a)

set_target_properties(
    expat PROPERTIES IMPORTED_LOCATION
    ${prebuilt_DIR}/${ANDROID_ABI}/lib/libexpat.a)

set(
    CMAKE_CXX_FLAGS
    "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${CMAKE_SOURCE_DIR})
set(
    HEADER_FILES
    cmdutils.h
    config.h
    ffmpeg.h)

add_library(ffmpegjni SHARED
            ffmpeg.c
            cmdutils.c
            ffmpeg_opt.c
            ffmpeg_filter.c)

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
    ass
    fontconfig
    freetype
    fribidi
    x264
    mp3lame
    png
    expat
    )