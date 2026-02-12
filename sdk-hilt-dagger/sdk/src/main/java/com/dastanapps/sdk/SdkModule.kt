package com.dastanapps.sdk

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class SdkModule {
    @Binds
    abstract fun bindViewModelFactory(factory: SdkViewModelFactory): ViewModelProvider.Factory
}
