package com.dastanapps.gles.filters2

import com.dastanapps.gles.GLDrawer2D

/**
 * Created by dastaniqbal on 20/02/2018.

 * 20/02/2018 3:42
 */
object FilterFactory {
    private val NONE = 0
    private val BW = 1
    private val LOOKUP = 2
    private val MIRROR = 3
    private val NEGATE = 4
    private val WOBBLE = 5

    fun getFilter(filterId: Int): GLDrawer2D {
        return when (filterId) {
            NONE -> NoneFilter()
            BW -> BlackNWhiteFilter()
            LOOKUP -> LookUpFilter()
            MIRROR -> MirrorFilter()
            NEGATE -> NegateFilter()
            WOBBLE -> WobbleFilter()
            else -> {
                NoneFilter()
            }
        }
    }
}