package com.dastanapps.dagger2.manual.repo

import android.util.Log

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class UserRepo(localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    private val TAG = this::class.java.simpleName

    fun login() {
        Log.d(TAG, "Logging In")
        remoteDataSource.login()
    }
}