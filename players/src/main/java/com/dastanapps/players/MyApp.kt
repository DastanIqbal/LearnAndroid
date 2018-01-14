package com.dastanapps.players

import android.app.Application
import com.facebook.FacebookSdk

/**
 * Created by dastaniqbal on 12/01/2018.
 * dastanIqbal@marvelmedia.com
 * 12/01/2018 4:23
 */
class MyApp : Application() {

    companion object {
        lateinit var mInstance: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this);
        mInstance = this
    }
}