package com.dastanapps.processing

import android.text.TextUtils
import android.util.Log
import com.dastanapps.ffmpegjni.VideoKit
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by dastaniqbal on 18/07/2018.
 * dastanIqbal@marvelmedia.com
 * 18/07/2018 7:03
 */
object FFmpegExecutor {

    private var videoKit = VideoKit()

    fun execute(cmds: Array<String>, videoKitListener: VideoKit.IVideoKit): Int {
        videoKit.videoKitListener = videoKitListener
        videoKit.setDebug(true)
        return videoKit.run(cmds)
    }

    fun execute(cmds: Array<String>): Observable<String> {
        return Observable.create<String> {
            videoKit.videoKitListener = object : VideoKit.IVideoKit {
                override fun progress(progress: String) {
                    it.onNext(progress)
                    Log.d("JNI:FFmpegExecutor Next", progress)
                }
            }
            videoKit.setDebug(true)
            val result = videoKit.run(cmds)
            if (result == 0) it.onComplete()
            else it.onError(Throwable("FFmpeg command failed"))
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