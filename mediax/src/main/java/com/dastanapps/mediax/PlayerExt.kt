package com.dastanapps.mediax

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
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

@UnstableApi
open class PlayerExt(val context: Context) {

    internal var currentMediaItemIndex: Int = 0

    protected lateinit var mediaSession: MediaLibrarySession

    private val playerListener by lazy { PlayerEventListener(this) }

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val replaceableForwardingPlayer: ReplaceableForwardingPlayer by lazy {
        ReplaceableForwardingPlayer(exoPlayer)
    }

    private val exoPlayer: Player by lazy {
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
        preparePlayerForResumption()
        mediaSession()
    }

    private fun preparePlayerForResumption() {
        val playableMediaItem = mediaItem()
        val startPositionMs =
            playableMediaItem.mediaMetadata.extras?.getLong(
                MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
            ) ?: 0
        playableMediaItem?.let {
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

    private fun mediaItem(): MediaItem {
//        val jsonImageUri = Uri.parse(song.image)
//        val imageUri = AlbumArtContentProvider.mapUri(jsonImageUri)
//        val mediaMetadata = MediaMetadata.Builder()
//            .from(song)
//            .apply {
//                setArtworkUri(imageUri) // Used by ExoPlayer and Notification
//                // Keep the original artwork URI for being included in Cast metadata object.
//                val extras = Bundle()
//                extras.putString(ORIGINAL_ARTWORK_URI_KEY, jsonImageUri.toString())
//                setExtras(extras)
//            }
//            .build()
        return MediaItem.Builder()
            .apply {
                setMediaId("wake_up_01")
                setUri("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/01_-_Intro_-_The_Way_Of_Waking_Up_feat_Alan_Watts.mp3")
                setMimeType(MimeTypes.AUDIO_MPEG)
//                setMediaMetadata(mediaMetadata)
            }.build()
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
        return MusicServiceCallback(context)
    }

    fun destroy(){
        replaceableForwardingPlayer.release()
        releaseMediaSession()
    }
}

val PLAYER_TAG = "PlayerExt"
const val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"