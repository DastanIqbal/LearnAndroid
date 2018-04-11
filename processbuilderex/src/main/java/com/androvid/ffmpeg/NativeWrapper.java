package com.androvid.ffmpeg;

import android.content.Context;
import android.util.Log;

public class NativeWrapper {
    protected static NativeWrapper a = null;
    public static boolean b = false;
    private String c = null;
    private String d = null;
    private StringBuilder e;
    private boolean f = false;

    private native void cancelAction();

    private native Object getAVInfo(String str);

    private native int getProgress();

    private native int loadFFMPEGLibrary(String str);

    private native int run(String[] strArr);

    private native void unloadFFMPEGLibrary();

    public native void setAudioProgress();

    protected NativeWrapper() {
        Log.d("DEBUG", "NativeWrapper.NativeWrapper");
        this.e = new StringBuilder();
    }

    public static NativeWrapper a() {
        if (a == null) {
            a = new NativeWrapper();
        }
        return a;
    }

    public void a(Context context) {
        Log.d("DEBUG", "NativeWrapper.initFFMPEG");
        if (this.c == null) {
            this.c = context.getApplicationInfo().dataDir + "/lib/libffmpeg.so";
            Log.d("DEBUG", "FFMPEG .so file: " + this.c);
        }
        if (this.d == null) {
            this.d = context.getApplicationInfo().dataDir + "/lib/libvideokit.so";
            Log.d("DEBUG", "VIDEOKIT .so file: " + this.d);
        }
    }

    public void c() {
        int loadFFMPEGLibrary;
        try {
            //unloadFFMPEGLibrary();
            loadFFMPEGLibrary = loadFFMPEGLibrary(this.c);
        } catch (Throwable th) {
            Log.d("DEBUG", "NativeWrapper.ffmpegRun: " + th.toString());
            loadFFMPEGLibrary = -1;
        }

        if (loadFFMPEGLibrary != 0) {
            Log.d("DEBUG","NativeWrapper.loadFFMPEGLibrary NORMAL failed!");
           // unloadFFMPEGLibrary();
        }
    }
}