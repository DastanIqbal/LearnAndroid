package com.dastanapps.sdk

import com.dastanapps.hilt.GreetingModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [GreetingModule::class, SdkModule::class])
interface SdkComponent {
    fun getGreeting(): String
    fun getViewModelFactory(): SdkViewModelFactory

    fun inject(activity: SdkActivity)

    companion object {
        fun create(): SdkComponent = DaggerSdkComponent.create()
    }
}

object SdkMiddleware {
    private var component: SdkComponent? = null

    fun init() {
        if (component == null) {
            component = SdkComponent.create()
        }
    }

    fun getComponent(): SdkComponent {
        if (component == null) {
            init()
        }
        return component!!
    }
}
