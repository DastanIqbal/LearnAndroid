package com.dastanapps.socketio

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlin.math.pow

class SocketIoManager(
    val url: String
) {
    private val LOG_TAG = "SocketIoManager"
    private val socket: Socket = IO.socket(url, IO.Options().apply {
        this.transports = arrayOf("websocket")
        this.auth = hashMapOf("token" to "")
    })
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    init {
        subscribe("student_trip_status"){
            Log.d(LOG_TAG, "student_trip_status ")
        }
        subscribe(Socket.EVENT_CONNECT){
            emit("subscribe:student", hashMapOf("token" to "", "studentId" to 1443)){ args ->
                Log.d(LOG_TAG, "subscribe:student: $args")
            }
        }
        subscribe(Socket.EVENT_DISCONNECT) {
            Log.d(LOG_TAG, "Disconnected from $url")
            disconnect()
            reconnect(url)
        }
        connect()
    }


    fun emit(event:String, data: Any, block: (Any) ->Unit){
        socket.emit(event, data, block)
    }

    fun connect(){
        socket.connect()
    }

    fun disconnect(){
        socket.off()
        socket.disconnect()
    }

    fun subscribe(event: String, block: () ->Unit ){
        socket.on(event){
            block.invoke()
        }
    }

    private fun reconnect(url: String) {
        scope.launch {
            flow<Unit> {
                socket.connect()
            }.retryWhen { cause, attempt ->
                delay((2.0.pow(attempt.toInt()) * 1000).toLong())
                socket.connect()
                true
            }
        }
    }
}