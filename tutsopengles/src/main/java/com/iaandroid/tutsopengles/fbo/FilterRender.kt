package com.iaandroid.tutsopengles.fbo

import com.dastanapps.mediasdk.opengles.gpu.fbo.LruBitmapCache

/**
 * 滤镜渲染器
 *
 *
 * 所有滤镜的父类
 * 继承自FBORender，支持设置图片缓存
 *
 * @author like
 * @date 2017-09-15
 */
open class FilterRender : FBORender() {

    open var bitmapCache = LruBitmapCache.getSingleInstance()
}
