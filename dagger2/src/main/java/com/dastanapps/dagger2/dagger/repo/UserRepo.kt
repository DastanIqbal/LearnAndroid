package com.dastanapps.dagger2.dagger.repo

import android.util.Log
import com.dastanapps.dagger2.dagger.di.MyCustomScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
@MyCustomScope
class UserRepo @Inject constructor(localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    private val TAG = this::class.java.simpleName

    fun login() {
        Log.d(TAG, "Dagger Logging In")
        remoteDataSource.login()
    }
}