package com.dastanapps.androidp.subscription

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by dastaniqbal on 19/03/2019.
 * 19/03/2019 12:39
 */
class PlayerLifecycleObserver : LifecycleObserver {
    private val TAG = this::class.java.simpleName

    companion object {
        val INSTANCE = PlayerLifecycleObserver()
    }

    private var actionHandler: KViewActionHandler? = null

    fun registerViewActionHandler(actionHandler: KViewActionHandler) {
        this.actionHandler = actionHandler
    }

    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        actionHandler?.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        actionHandler?.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        actionHandler?.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        actionHandler?.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        actionHandler = null
    }
}