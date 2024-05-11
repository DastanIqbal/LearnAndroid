package com.dastanapps.mediax

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaLibraryService.MediaLibrarySession

//import com.tencent.tmf.mini.api.TmfMiniSDK;


/**
 *
 * Created by Iqbal Ahmed on 06/05/2024
 *
 */

inline val Player.isPlayEnabled
    get() = (availableCommands.contains(Player.COMMAND_PLAY_PAUSE)) &&
            (!playWhenReady)

inline val Player.isEnded
    get() = playbackState == Player.STATE_ENDED


const val ORIGINAL_ARTWORK_URI_KEY = "com.example.android.uamp.JSON_ARTWORK_URI"
fun mediaItem(): List<MediaItem> {
        val jsonImageUri = Uri.parse("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/art.jpg")
//        val imageUri = AlbumArtContentProvider.mapUri(jsonImageUri)
        val mediaMetadata = MediaMetadata.Builder()
//            .from(song)
            .apply {
                setIsPlayable(true)
                setIsBrowsable(true)
                setArtworkUri(jsonImageUri) // Used by ExoPlayer and Notification
//                // Keep the original artwork URI for being included in Cast metadata object.
                val extras = Bundle()
                extras.putString(ORIGINAL_ARTWORK_URI_KEY, jsonImageUri.toString())
                setExtras(extras)
            }
            .build()

    val mutableList = arrayListOf<MediaItem>()
    mutableList.add(
        MediaItem.Builder()
        .apply {
            setMediaId("wake_up_01")
            setUri("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/01_-_Intro_-_The_Way_Of_Waking_Up_feat_Alan_Watts.mp3")
            setMimeType(MimeTypes.AUDIO_MPEG)
            setMediaMetadata(mediaMetadata)
        }.build()
    )
    mutableList.add(
        MediaItem.Builder()
            .apply {
                setMediaId("wake_up_02")
                setUri("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3")
                setMimeType(MimeTypes.AUDIO_MPEG)
                setMediaMetadata(mediaMetadata)
            }.build()
    )
    mutableList.add(
        MediaItem.Builder()
            .apply {
                setMediaId("wake_up_03")
                setUri("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/03_-_Voyage_I_-_Waterfall.mp3")
                setMimeType(MimeTypes.AUDIO_MPEG)
                setMediaMetadata(mediaMetadata)
            }.build()
    )
    mutableList.add(
        MediaItem.Builder()
            .apply {
                setMediaId("wake_up_04")
                setUri("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/04_-_The_Music_In_You.mp3")
                setMimeType(MimeTypes.AUDIO_MPEG)
                setMediaMetadata(mediaMetadata)
            }.build()
    )

    return mutableList
}

@UnstableApi
open class PlayerExt(val context: Context) {

    internal var currentMediaItemIndex: Int = 0

    protected lateinit var mediaSession: MediaLibrarySession

    internal val packageValidator: PackageValidator by lazy {
        PackageValidator(context, R.xml.allowed_media_browser_callers)
    }

    private val playerListener by lazy { PlayerEventListener() }

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val replaceableForwardingPlayer: ReplaceableForwardingPlayer by lazy {
        ReplaceableForwardingPlayer(exoPlayer)
    }

    internal val exoPlayer: Player by lazy {
        val player = ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
        player.addAnalyticsListener(EventLogger(null, "exoplayer-uamp"))
        player
    }

    fun playerController() = replaceableForwardingPlayer

    fun playerSession()  = mediaSession

    init {
//        preparePlayerForResumption()
        mediaSession()
    }

    internal fun preparePlayerForResumption(item: MediaItem) {
        val playableMediaItem = item
        val startPositionMs =
            playableMediaItem.mediaMetadata.extras?.getLong(
                MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
            ) ?: 0
        playableMediaItem.let {
            exoPlayer.setMediaItem(playableMediaItem)
            exoPlayer.seekTo(startPositionMs)
            exoPlayer.prepare()
        }
    }

    private fun mediaSession(){
        mediaSession = with(MediaLibrarySession.Builder(
            context, replaceableForwardingPlayer, getCallback())) {
            setId(context.packageName)
            context.packageManager?.getLaunchIntentForPackage(context.packageName)?.let { sessionIntent ->
                setSessionActivity(
                    PendingIntent.getActivity(
                        /* context= */ context,
                        /* requestCode= */ 0,
                        sessionIntent,
                        FLAG_IMMUTABLE
                    )
                )
            }
            build()
        }
    }
    internal fun saveRecentSongToStorage() {
    }

    internal fun releaseMediaSession() {
        mediaSession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.removeListener(playerListener)
                player.release()
            }
        }

        // Cancel coroutines when the service is going away.
//        serviceJob.cancel()
    }

    open fun getCallback(): MediaLibrarySession.Callback {
        return MusicServiceCallback(context, this)
    }

    fun destroy(){
        replaceableForwardingPlayer.release()
        releaseMediaSession()
    }

    /** Listen for events from ExoPlayer. */
    private inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(EVENT_POSITION_DISCONTINUITY)
                || events.contains(EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                currentMediaItemIndex = player.currentMediaItemIndex
                saveRecentSongToStorage()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = " R.string.generic_error";
            Log.e(
                TAG_PLAYER,
                "Player error: " + error.errorCodeName + " (" + error.errorCode + ")",
                error
            );
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = "R.string.error_media_not_found";
            }
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

val TAG_PLAYER = "PlayerExt"
const val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"