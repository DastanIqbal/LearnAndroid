package com.dastanapps.dagger2.dagger.di

import dagger.Component

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
@MyCustomScope
@Component(modules = [NetworkModule::class, SubComponentModules::class])
interface AppGraph {

    fun loginComponent(): LoginComponent.Factory
}