package com.dastanapps

import android.os.Handler
import android.os.Message

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 11:15
 */
object CommonUtils {

    fun sendMessageToHandler(handler: Handler, what: Int, msg: Any) {
        val message = Message()
        message.what = what
        message.obj = msg
        handler.sendMessage(message)
    }
}