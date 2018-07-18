package com.dastanapps.ffmpegso;

import processing.ffmpeg.videokit.CommandBuilder;
import processing.ffmpeg.videokit.LogLevel;
import processing.ffmpeg.videokit.VideoCommandBuilder;

/**
 * Created by Ilja Kosynkin on 06.07.2016.
 * Copyright by inFullMobile
 */
public class VideoKit {
    static {
        try {
            System.loadLibrary("ffmpegso");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    private LogLevel logLevel = LogLevel.NO_LOG;

    public void setLogLevel(LogLevel level) {
        logLevel = level;
    }

    public int process(String[] args) {
        return run(args);
    }

    // JNI
    public native String avformatinfo();

    public native String avcodecinfo();

    public native String avfilterinfo();

    public native String configurationinfo();

    public native int run(String[] args);

    public CommandBuilder createCommand() {
        return new VideoCommandBuilder(this);
    }
}
