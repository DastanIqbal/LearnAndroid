package com.dastanapps.mediax.ui

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
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
import com.dastanapps.mediax.TAG_PLAYER
import com.dastanapps.mediax.R
import com.dastanapps.mediax.databinding.ActivityMainBinding
import com.dastanapps.mediax.isEnded
import com.dastanapps.mediax.isPlayEnabled
import com.dastanapps.mediax.mediaItem
import com.dastanapps.mediax.provideMusicServiceConnection
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

// https://github.com/androidx/media/blob/release/demos/session/src/main/java/androidx/media3/demo/session/PlayerActivity.kt

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null
    private lateinit var playerView: PlayerView
    private lateinit var binding: ActivityMainBinding

    private val musicServiceConnection by lazy {
        provideMusicServiceConnection(this)
    }

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

    override fun onResume() {
        super.onResume()
        playMedia(mediaItem()[0], false)
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

    fun playMedia(
        mediaItem: MediaItem,
        pauseThenPlaying: Boolean = true,
        parentMediaId: String = "songs"
    ) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val player = musicServiceConnection.player?: return

        val isPrepared = player.playbackState != Player.STATE_IDLE
        if (isPrepared && mediaItem.mediaId == nowPlaying?.mediaId) {
            when {
                player.isPlaying ->
                    if (pauseThenPlaying) player.pause() else Unit
                player.isPlayEnabled -> player.play()
                player.isEnded -> player.seekTo(C.TIME_UNSET)
                else -> {
                    Log.w(
                        TAG_PLAYER, "Playable item clicked but neither play nor pause are enabled!" +
                                " (mediaId=${mediaItem.mediaId})"
                    )
                }
            }
        } else {
            lifecycleScope.launchWhenResumed {
                var playlist: MutableList<MediaItem> = arrayListOf()
                // load the children of the parent if requested
                parentMediaId?.let {
                    playlist = musicServiceConnection.getChildren(parentMediaId).let { children ->
                        children.filter {
                            it.mediaMetadata.isPlayable ?: false
                        }
                    }.toMutableList()
                }
                if (playlist.isEmpty()) {
                    playlist.add(mediaItem)
                }
                val indexOf = playlist.indexOf(mediaItem)
                val startWindowIndex = if (indexOf >= 0) indexOf else 0
                player.setMediaItems(
                    playlist, startWindowIndex, /* startPositionMs= */ C.TIME_UNSET
                )
                player.prepare()
                player.play()
            }
        }
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