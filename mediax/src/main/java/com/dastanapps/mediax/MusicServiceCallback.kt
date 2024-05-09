package com.dastanapps.mediax

import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.max

class MusicServiceCallback(
    private val context: Context
): MediaLibrarySession.Callback {

//    override fun onGetLibraryRoot(
//        session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?
//    ): ListenableFuture<LibraryResult<MediaItem>> {
//        // By default, all known clients are permitted to search, but only tell unknown callers
//        // about search if permitted by the [BrowseTree].
//        val isKnownCaller = context.packageValidator.isKnownCaller(browser.packageName, browser.uid)
//        val rootExtras = Bundle().apply {
//            putBoolean(
//                MEDIA_SEARCH_SUPPORTED,
//                isKnownCaller || browseTree.searchableByUnknownCaller
//            )
//            putBoolean(CONTENT_STYLE_SUPPORTED, true)
//            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
//            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
//        }
//        val libraryParams = LibraryParams.Builder().setExtras(rootExtras).build()
//        val rootMediaItem = if (!isKnownCaller) {
//            MediaItem.EMPTY
//        } else if (params?.isRecent == true) {
//            if (exoPlayer.currentTimeline.isEmpty) {
//                storage.loadRecentSong()?.let {
//                    preparePlayerForResumption(it)
//                }
//            }
//            recentRootMediaItem
//        } else {
//            catalogueRootMediaItem
//        }
//        return Futures.immediateFuture(LibraryResult.ofItem(rootMediaItem, libraryParams))
//    }

//    override fun onGetChildren(
//        session: MediaLibrarySession,
//        browser: MediaSession.ControllerInfo,
//        parentId: String,
//        page: Int,
//        pageSize: Int,
//        params: LibraryParams?
//    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
//        if (parentId == recentRootMediaItem.mediaId) {
//            return Futures.immediateFuture(
//                LibraryResult.ofItemList(
//                    storage.loadRecentSong()?.let {
//                            song -> listOf(song)
//                    }!!,
//                    LibraryParams.Builder().build()
//                )
//            )
//        }
//        return callWhenMusicSourceReady {
//            LibraryResult.ofItemList(
//                browseTree[parentId] ?: ImmutableList.of(),
//                LibraryParams.Builder().build()
//            )
//        }
//    }

//    override fun onGetItem(
//        session: MediaLibrarySession,
//        browser: MediaSession.ControllerInfo,
//        mediaId: String
//    ): ListenableFuture<LibraryResult<MediaItem>> {
//        return callWhenMusicSourceReady {
//            LibraryResult.ofItem(
//                browseTree.getMediaItemByMediaId(mediaId) ?: MediaItem.EMPTY,
//                LibraryParams.Builder().build())
//        }
//    }

//    override fun onSearch(
//        session: MediaLibrarySession,
//        browser: MediaSession.ControllerInfo,
//        query: String,
//        params: LibraryParams?
//    ): ListenableFuture<LibraryResult<Void>> {
//        return callWhenMusicSourceReady {
//            val searchResult = musicSource.search(query, params?.extras ?: Bundle())
//            mediaSession.notifySearchResultChanged(browser, query, searchResult.size, params)
//            LibraryResult.ofVoid()
//        }
//    }

//    override fun onGetSearchResult(
//        session: MediaLibrarySession,
//        browser: MediaSession.ControllerInfo,
//        query: String,
//        page: Int,
//        pageSize: Int,
//        params: LibraryParams?
//    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
//        return callWhenMusicSourceReady {
//            val searchResult = musicSource.search(query, params?.extras ?: Bundle())
//            val fromIndex = max((page - 1) * pageSize, searchResult.size - 1)
//            val toIndex = max(fromIndex + pageSize, searchResult.size)
//            LibraryResult.ofItemList(searchResult.subList(fromIndex, toIndex), params)
//        }
//    }

//    override fun onAddMediaItems(
//        mediaSession: MediaSession,
//        controller: MediaSession.ControllerInfo,
//        mediaItems: MutableList<MediaItem>
//    ): ListenableFuture<MutableList<MediaItem>> {
//        return callWhenMusicSourceReady {
//            mediaItems.map { browseTree.getMediaItemByMediaId(it.mediaId)!! }.toMutableList()
//        }
//    }
//
//    override fun onCustomCommand(
//        session: MediaSession,
//        controller: MediaSession.ControllerInfo,
//        customCommand: SessionCommand,
//        args: Bundle
//    ): ListenableFuture<SessionResult> {
//        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
//    }
}