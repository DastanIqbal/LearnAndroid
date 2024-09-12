package com.dastanapps.tencent_miniapp

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: App
    }
}