package com.dastanapps.dagger2

import android.app.Application
import com.dastanapps.dagger2.manual.AppContainer

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class App : Application() {
    private val TAG = this::class.java.simpleName
    val appContainer = AppContainer()
}