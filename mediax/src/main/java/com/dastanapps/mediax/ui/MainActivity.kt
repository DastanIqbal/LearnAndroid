package com.dastanapps.mediax.ui

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_MEDIA_METADATA_CHANGED
import androidx.media3.common.Player.EVENT_TIMELINE_CHANGED
import androidx.media3.common.Player.EVENT_TRACKS_CHANGED
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.dastanapps.mediax.MusicService
import com.dastanapps.mediax.PlayerExt
import com.dastanapps.mediax.R
import com.dastanapps.mediax.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

// https://github.com/androidx/media/blob/release/demos/session/src/main/java/androidx/media3/demo/session/PlayerActivity.kt

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null
    private lateinit var playerView: PlayerView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerView = binding.playerView
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        super.onStop()
        playerView.player = null
        releaseController()
    }

    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, MusicService::class.java)),
            )
                .buildAsync()
        updateMediaMetadataUI()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun setController() {
        val controller = this.controller ?: return

        playerView.player = controller

        updateCurrentPlaylistUI()
        updateMediaMetadataUI()
        playerView.setShowSubtitleButton(controller.currentTracks.isTypeSupported(TRACK_TYPE_TEXT))

        controller.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.contains(EVENT_TRACKS_CHANGED)) {
                        playerView.setShowSubtitleButton(player.currentTracks.isTypeSupported(TRACK_TYPE_TEXT))
                    }
                    if (events.contains(EVENT_TIMELINE_CHANGED)) {
                        updateCurrentPlaylistUI()
                    }
                    if (events.contains(EVENT_MEDIA_METADATA_CHANGED)) {
                        updateMediaMetadataUI()
                    }
                    if (events.contains(EVENT_MEDIA_ITEM_TRANSITION)) {
//                        // Trigger adapter update to change highlight of current item.
//                        mediaItemListAdapter.notifyDataSetChanged()
                    }
                }
            }
        )
    }

    private fun updateMediaMetadataUI() {
        val controller = this.controller
        if (controller == null || controller.mediaItemCount == 0) {
            binding.mediaTitle.text = "Waiting for metadata"
            binding.mediaArtist.text = ""
            return
        }

        val mediaMetadata = controller.mediaMetadata
        val title: CharSequence = mediaMetadata.title ?: ""

        binding.mediaTitle.text = title
        binding.mediaArtist.text = mediaMetadata.artist
    }

    private fun updateCurrentPlaylistUI() {
        val controller = this.controller ?: return
//        mediaItemList.clear()
        for (i in 0 until controller.mediaItemCount) {
//            mediaItemList.add(controller.getMediaItemAt(i))
        }
//        mediaItemListAdapter.notifyDataSetChanged()
    }
}