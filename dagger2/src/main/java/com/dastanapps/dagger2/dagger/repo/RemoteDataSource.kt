package com.dastanapps.dagger2.dagger.repo

import android.util.Log
import javax.inject.Inject

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class RemoteDataSource @Inject constructor(/*retrofit: Retrofit*/) {
    private val TAG = this::class.java.simpleName

    fun login() {
        Log.d(TAG, "Dagger LogIn Request")
    }
}