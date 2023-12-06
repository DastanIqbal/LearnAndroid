package com.dastanapps.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiExt {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()


    fun <T> jsonToClass(json: String, clazz: Class<T>): T? {
        val adapter = moshi.adapter(clazz)
        return adapter.fromJson(json)
    }

    fun <T> classToJson(clazz: T, adapt: Class<T>): String? {
        val adapter = moshi.adapter(adapt)
        return adapter.toJson(clazz)
    }

    fun <T> jsonToClass(json: String, adapter: JsonAdapter<T>): T? {
        return adapter.fromJson(json)
    }
}