package com.dastanapps.mediax

import android.content.Context
import android.os.Bundle
import android.os.ConditionVariable
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Executors
import kotlin.math.max

const val UAMP_RECENT_ROOT = "__RECENT__"
const val UAMP_BROWSABLE_ROOT = "__SONGS__"

@UnstableApi
class MusicServiceCallback(
    private val context: Context,
    private val musicService: PlayerExt
): MediaLibrarySession.Callback {

    private val browseTree: BrowseTree by lazy { BrowseTree() }

    private val executorService by lazy {
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())
    }

    private fun openWhenReady(conditionVariable: ConditionVariable): (Boolean) -> Unit = {
        val successfullyInitialized = it
        if (!successfullyInitialized) {
            Log.e(TAG_PLAYER, "loading music source failed")
        }
        conditionVariable.open()
    }

    private fun <T> callWhenMusicSourceReady(action: () -> T): ListenableFuture<T> {
//        val conditionVariable = ConditionVariable()
//        return if (musicSource.whenReady(openWhenReady(conditionVariable))) {
//            Futures.immediateFuture(action())
//        } else {
//            executorService.submit<T> {
//                conditionVariable.block();
//                action()
//            }
//        }

        return executorService.submit<T> {
//            conditionVariable.block();
            action()
        }
    }

    private val recentRootMediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId(UAMP_RECENT_ROOT)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setIsPlayable(false)
                    .build())
            .build()
    }

    private val catalogueRootMediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId(UAMP_BROWSABLE_ROOT)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setIsPlayable(false)
                    .build())
            .build()
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        // By default, all known clients are permitted to search, but only tell unknown callers
        // about search if permitted by the [BrowseTree].
        val isKnownCaller = musicService.packageValidator.isKnownCaller(browser.packageName, browser.uid)
        val rootExtras = Bundle().apply {
//            putBoolean(
//                MEDIA_SEARCH_SUPPORTED,
//                isKnownCaller || browseTree.searchableByUnknownCaller
//            )
//            putBoolean(CONTENT_STYLE_SUPPORTED, true)
//            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
//            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }
        val libraryParams = LibraryParams.Builder().setExtras(rootExtras).build()
        val rootMediaItem = if (!isKnownCaller) {
            MediaItem.EMPTY
        } else if (params?.isRecent == true) {
            if (musicService.exoPlayer.currentTimeline.isEmpty) {
                musicService.preparePlayerForResumption(mediaItem()[0])
            }
            recentRootMediaItem
        } else {
            catalogueRootMediaItem
        }
        return Futures.immediateFuture(LibraryResult.ofItem(rootMediaItem, libraryParams))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
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
        return callWhenMusicSourceReady {
            LibraryResult.ofItemList(
                browseTree[parentId] ?: ImmutableList.of(),
                LibraryParams.Builder().build()
            )
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return callWhenMusicSourceReady {
            LibraryResult.ofItem(
                browseTree.getMediaItemByMediaId(mediaId) ?: MediaItem.EMPTY,
                LibraryParams.Builder().build())
        }
    }

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

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        return callWhenMusicSourceReady {
            mediaItems.map { browseTree.getMediaItemByMediaId(it.mediaId)!! }.toMutableList()
        }
    }
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