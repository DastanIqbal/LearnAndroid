package com.dastanapps.mediax

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@UnstableApi
class MusicService() : MediaSessionService() {

    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private lateinit var packageValidator: PackageValidator

    private val playerExt by lazy {
        PlayerExt(this)
    }

    private val player get() = playerExt.playerController()

    override fun onCreate() {
        super.onCreate()
        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
        player.playWhenReady = true
    }

//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
//        return if ("android.media.session.MediaController" == controllerInfo.packageName
//            || packageValidator.isKnownCaller(controllerInfo.packageName, controllerInfo.uid)) {
//            playerExt.playerSession()
//        } else null
//    }

    /** Called when swiping the activity away from recents. */
    override fun onTaskRemoved(rootIntent: Intent) {
//        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)
        // The choice what to do here is app specific. Some apps stop playback, while others allow
        // playback to continue and allow users to stop it with the notification.
        playerExt.releaseMediaSession()
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
       return playerExt.playerSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerExt.destroy()
    }

    companion object{

        fun instance(context: Context){
            context.startService(Intent(context, MusicService::class.java))
        }
    }
}