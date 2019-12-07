package com.dastanapps.dagger2.dagger.di

import com.dastanapps.dagger2.dagger.repo.UserRepo
import dagger.Component
import javax.inject.Singleton

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
@MyCustomScope
@Component
interface AppGraph {

    fun userRepo(): UserRepo
}