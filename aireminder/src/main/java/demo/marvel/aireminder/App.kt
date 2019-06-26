package demo.marvel.aireminder

import com.dastanapps.dastanlib.DastanLibApp

/**
 * Created by dastaniqbal on 19/06/2019.
 * 19/06/2019 5:01
 */
class App : DastanLibApp() {
    private val TAG = this::class.java.simpleName

    companion object {
        lateinit var INSTANCE: App
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}