package com.dastanapps.processing

import android.text.TextUtils
import android.util.Log
import com.dastanapps.ffmpegjni.FFmpegError
import com.dastanapps.ffmpegjni.VideoKit
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by dastaniqbal on 18/07/2018.

 * 18/07/2018 7:03
 */
object FFmpegExecutor {

    private var videoKit = VideoKit()

    fun execute(cmds: Array<String>, videoKitListener: VideoKit.IVideoKit) {
        videoKit.videoKitListener = videoKitListener
        videoKit.setDebug(true)
        videoKit.run(cmds)
    }

    fun executeProbe(cmds: Array<String>): Observable<String> {
        return Observable.create<String> {
            videoKit.videoKitProbeListener = object : VideoKit.IVideoKitProbe {
                override fun output(output: String) {
                    it.onNext(output)
                }

                override fun error(error: String) {
                    it.onError(FFmpegError(error))
                }
            }
            val result = videoKit.runprobe(cmds)
            if (result == 0) it.onComplete()
            //else it.onError(FFmpegError("FFmpeg command failed $result"))
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
    }

    fun execute(cmds: Array<String>): Observable<String> {
        return Observable.create<String> {
            videoKit.videoKitListener = object : VideoKit.IVideoKit {
                override fun result(result: Int) {
                    if (result == 0) {
                        it.onComplete()
                    } else /*if(!it.isDisposed)*/{
                        it.onError(FFmpegError("FFmpeg command failed $result"))
                    }
                }

                override fun error(error: String) {
                    it.onError(FFmpegError(error))
                }

                override fun benchmark(bench: String) {
                    it.onNext(bench)
                }

                override fun progress(progress: String) {
                    it.onNext(progress)
                    Log.d("JNI:FFmpegExecutor Next", progress)
                }
            }
            videoKit.setDebug(true)
            videoKit.run(cmds)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
    }

    fun avformatinfo(): Observable<String> {
        return createObserable { videoKit.avformatinfo() }
    }

    fun avcodecinfo(): Observable<String> {
        return createObserable { videoKit.avcodecinfo() }
    }

    fun avfilterinfo(): Observable<String> {
        return createObserable { videoKit.avfilterinfo() }
    }

    fun configurationinfo(): Observable<String> {
        return createObserable { videoKit.configurationinfo() }
    }

    fun stop(stop: Boolean) {
        videoKit.stopTranscoding(stop)
        videoKit.error("Video Stopped")
    }

    fun stopProbe() {
        videoKit.stopFFprobe()
    }

    private fun createObserable(func: (Unit) -> (String)): Observable<String> {
        return Observable.create<String> {
            val result = func.invoke(Unit)
            if (!TextUtils.isEmpty(result)) {
                it.onNext(result)
                it.onComplete()
            } else it.onError(Throwable("FFmpeg command failed"))
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
    }
}