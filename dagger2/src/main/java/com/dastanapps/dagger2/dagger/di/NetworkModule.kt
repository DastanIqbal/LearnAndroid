package com.dastanapps.dagger2.dagger.di

import com.dastanapps.dagger2.dagger.repo.Retrofit
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
@Module
class NetworkModule {
    private val TAG = this::class.java.simpleName

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit()
    }
}