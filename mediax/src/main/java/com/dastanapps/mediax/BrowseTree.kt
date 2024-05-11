package com.dastanapps.mediax

import androidx.media3.common.MediaItem

/**
 *
 * Created by Iqbal Ahmed on 12/05/2024
 *
 */

class BrowseTree {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaItem>>()
    private val mediaIdToMediaItem = mutableMapOf<String, MediaItem>()
    init {
        mediaIdToChildren["songs"] = mediaItem().toMutableList()
        mediaItem().forEach {
            mediaIdToMediaItem[it.mediaId] = it
        }
    }

    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]
    fun getMediaItemByMediaId(mediaId: String) = mediaIdToMediaItem[mediaId]
}