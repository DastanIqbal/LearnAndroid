package com.dastanapps

import android.app.Application

/**
 * Created by dastaniqbal on 01/02/2018.
 * dastanIqbal@marvelmedia.com
 * 01/02/2018 4:20
 */
class MyApp : Application() {

    companion object {
        var instance: MyApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}