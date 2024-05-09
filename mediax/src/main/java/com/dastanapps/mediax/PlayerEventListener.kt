package com.dastanapps.mediax

import android.util.Log
import android.widget.Toast
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY
import androidx.media3.common.util.UnstableApi


/** Listen for events from ExoPlayer. */
@UnstableApi
class PlayerEventListener(
    private val playerExt: PlayerExt
) : Player.Listener {
    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(EVENT_POSITION_DISCONTINUITY)
            || events.contains(EVENT_MEDIA_ITEM_TRANSITION)
            || events.contains(EVENT_PLAY_WHEN_READY_CHANGED)
        ) {
            playerExt.currentMediaItemIndex = player.currentMediaItemIndex
            playerExt.saveRecentSongToStorage()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        var message = " R.string.generic_error";
        Log.e(
            PLAYER_TAG,
            "Player error: " + error.errorCodeName + " (" + error.errorCode + ")",
            error
        );
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
        ) {
            message = "R.string.error_media_not_found";
        }
        Toast.makeText(
            playerExt.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}