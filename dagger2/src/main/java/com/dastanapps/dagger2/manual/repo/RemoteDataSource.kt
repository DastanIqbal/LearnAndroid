package com.dastanapps.dagger2.manual.repo

import android.util.Log

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class RemoteDataSource(retrofit: Retrofit) {
    private val TAG = this::class.java.simpleName

    fun login() {
        Log.d(TAG, "LogIn Request")
    }
}