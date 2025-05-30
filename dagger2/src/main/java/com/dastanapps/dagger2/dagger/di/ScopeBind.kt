package com.dastanapps.dagger2.dagger.di

import javax.inject.Scope

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class MyCustomScope

// Definition of a custom scope called ActivityScope
@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ActivityScope