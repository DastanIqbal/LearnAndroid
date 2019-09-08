package com.dastanapps.androidp.subscription

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.androidp.R
import kotlinx.android.synthetic.main.activity_subscription.*


class SubscriptionActivity : AppCompatActivity() {

    private var tryfreeDialog: TryFreeDialog? = null
    private val lifecycleObserver = PlayerLifecycleObserver()

    private val exoPlayerHelper by lazy {
        ExoPlayerHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        // or add <item name="android:windowTranslucentStatus">true</item> in the theme
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        window.decorView.setOnSystemUiVisibilityChangeListener {
            changeStatusBarColor("#ff0000")
        }
        val attrib = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        tryfreeDialog = TryFreeDialog(this)
        tryfreeDialog?.handleBack {
            finish()
        }

        lifecycleObserver.registerLifecycle(lifecycle)
        lifecycleObserver.registerViewActionHandler(object : KViewActionHandler {
            override fun onStart() {
                exoPlayerHelper.onStart()
                exoPlayerHelper.playfromRaw(R.raw.fullscreen_krusoplus, true)
                // exoPlayerHelper.playWhenReady(true)

                kvidview.setShutterBackgroundColor(Color.TRANSPARENT)
                kvidview.setKeepContentOnPlayerReset(true)
                kvidview.player = exoPlayerHelper.player
            }

            override fun onStop() {
                exoPlayerHelper.onStop()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayerHelper.playWhenReady(false)
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(color)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        changeStatusBarColor("#ff0000")
        // Enables regular immersive mode./
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                // or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                // or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                //  or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
