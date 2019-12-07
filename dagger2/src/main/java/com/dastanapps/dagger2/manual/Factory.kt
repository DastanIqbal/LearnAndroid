package com.dastanapps.dagger2.manual

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
interface Factory<T> {
    fun create(): T
}