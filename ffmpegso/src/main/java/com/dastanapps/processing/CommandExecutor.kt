package com.dastanapps.processing

import com.dastanapps.ffmpegso.VideoKit
import io.reactivex.Observable

/**
 * Created by dastaniqbal on 18/07/2018.
 * dastanIqbal@marvelmedia.com
 * 18/07/2018 7:03
 */
class CommandExecutor {
    var videoKit = VideoKit()

    fun execute(cmds: Array<String>): Observable<String>? {
        return Observable.create<String> {
            val result = videoKit.run(cmds)
            if (result == 0) it.onComplete()
            else it.onError(Throwable("FFmpeg command failed"))
        }
    }
}