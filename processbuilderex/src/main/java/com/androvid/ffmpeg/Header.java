package com.androvid.ffmpeg;


public class Header {

    private native void cancelAction();

    private native Object getAVInfo(String str);

    private native int getProgress();

    private native int loadFFMPEGLibrary(String str);

    private native int run(String[] strArr);

    private native void unloadFFMPEGLibrary();

    public native void setAudioProgress();
}
