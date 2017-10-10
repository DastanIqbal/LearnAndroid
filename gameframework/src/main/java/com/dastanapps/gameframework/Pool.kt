package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 09/10/2017.
 * dastanIqbal@marvelmedia.com
 * 09/10/2017 11:33
 */
class Pool<T>(val factory: PoolObjectFactory<T>, val maxSize: Int) {
    interface PoolObjectFactory<T> {
        fun createObject(): T
    }

    private var freeObject = ArrayList<T>(maxSize)

    fun newObject(): T {
        var obj: T? = null
        obj = if (freeObject.isEmpty()) {
            factory.createObject()
        } else {
            freeObject.removeAt(freeObject.size - 1)
        }
        return obj
    }

    fun freeObject(obj: T) {
        if (freeObject.size < maxSize) {
            freeObject.add(obj)
        }
    }
}