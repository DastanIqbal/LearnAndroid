package com.dastanapps.youtubeiframe

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.dastanapps.dastanlib.utils.CommonUtils
import com.dastanapps.testcode.R
import kotlinx.android.synthetic.main.activity_youtube_iframe.*
import kotlinx.android.synthetic.main.fragment_youtube_iframe.*
import org.json.JSONObject
import java.util.*




class YoutubeIFrameActivity : AppCompatActivity() {
    class YTDialogFragment : DialogFragment() {

        lateinit var webView: WebView

        private fun iframeConfig(videoId: String, start: Int): JSONObject? {
            return try {
                val eventObject = JSONObject()
                eventObject.put("onReady", "onPlayerReady")
                        .put("onError", "onPlayerError")
                        .put("onStateChange", "onPlayerStateChange")

                val playerVarsObject = JSONObject()
                playerVarsObject.put("start", start)
                        .put("rel", 0)
                        .put("showinfo", 0)
                        .put("modestbranding", 0)
                        .put("iv_load_policy", 3)
                        .put("autohide", 1)
                        .put("autoplay", 1)
                        .put("cc_load_policy", 1)
                        .put("playsinline", 1)
                        .put("controls", 0)

                JSONObject().put("videoId", videoId)
                        .put("events", eventObject)
                        .put("height", "100%")
                        .put("width", "100%")
                        .put("playerVars", playerVarsObject)
            } catch (e: Throwable) {
                Log.e("YT", e.message)
                null
            }

        }

        private fun loadIframe(): String? {
            return try {
                CommonUtils.readFromRawRes(activity!!, com.dastanapps.testcode.R.raw.youtube_player_iframe)
            } catch (e: Throwable) {
                Log.e("YT", e.message + "")
                log("Unable to load youtube html frame.", false)
                null
            }

        }

        var c = 0
        var e = false
        var d = -9223372036854775807L

        private inner class JsInterface {

            @JavascriptInterface
            fun postPlayerEvent(i: Int, i2: Int) {
                if (i2 in 0..2147482) {
                    var stringBuilder = StringBuilder("InlineYoutubeVideoPlayer - YoutubeJsInterface postPlayerEvent:")
                    stringBuilder.append(i)
                    stringBuilder.append(" data: ")
                    stringBuilder.append(i2)
                    Log.i("YT", stringBuilder.toString())
                    var z = true
                    val stringBuilder2: String
                    when (i) {
                        0 -> {
                         //   myRl?.a(i2)
                            return
                        }
                        1 -> {
                            c = i2 * 1000
                            return
                        }
                        2 -> {
                            val j = (i2 * 1000).toLong()
                            Log.d("YT", "$i2 Playback States")
                            if (!e && d == -9223372036854775807L) {
                                d = j
                                Handler(Looper.getMainLooper()).post {
                                    webView.loadUrl("javascript:(function() { loaded = true; })()")
                                    myRl?.invalidate()
                                }
                                playing = true
                                hidePlayButton()
                                if (d <= 0) {
                                    val stringBuilder3 = StringBuilder("Invalid duration=")
                                    stringBuilder3.append(d)
                                    log(stringBuilder3.toString(), false)
                                }
                                return
                            }
                            return
                        }
                        3 -> {
                            stringBuilder = StringBuilder("Youtube player Error=")
                            stringBuilder.append(i2)
                            stringBuilder2 = stringBuilder.toString()
                            if (i2 != 0) {
                                z = false
                            }
                            log(stringBuilder2, z)
                            return
                        }
                        else -> {
                            stringBuilder = StringBuilder("Invalid postPlayerEvent")
                            stringBuilder.append(i2)
                            stringBuilder2 = stringBuilder.toString()
                            if (i2 != 0) {
                                z = false
                            }
                            log(stringBuilder2, z)
                            return
                        }
                    }
                }
            }
        }

        private fun showPlayButton() {
            imv_play.visibility = View.VISIBLE
            hidePlayButton()
        }

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = {
            if (imv_play != null)
                imv_play.visibility = View.GONE
        }

        private fun hidePlayButton() {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 3000)
        }

        fun log(str: String, z: Boolean) {
            val stringBuilder = StringBuilder("InlineYoutubeVideoPlayer: ")
            stringBuilder.append(str)
        }

        companion object {
            fun getInstance(bundle: Bundle): YTDialogFragment {
                val dialogFragment = YTDialogFragment()
                dialogFragment.arguments = bundle
                return dialogFragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.window?.run {
                requestFeature(Window.FEATURE_NO_TITLE)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            return dialog
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_youtube_iframe, container)
        }

        var playing = false
        private var myRl: MyRelativeLayout? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val iframe = loadIframe()
            myRl = MyRelativeLayout(activity!!)
            myRl?.setBackgroundColor(Color.BLACK)
            webView = WebView(activity!!)

            myRl?.clipChildren = true

            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            layoutParams.gravity=Gravity.CENTER
            myRl?.addView(webView, 0,layoutParams)
            /*fl.addView(webView,0, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 660))*/

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.mediaPlaybackRequiresUserGesture = false
            webView.settings.userAgentString = WebSettings.getDefaultUserAgent(activity!!)
            webView.settings.allowFileAccess = false
            webView.settings.allowUniversalAccessFromFileURLs = false
            webView.addJavascriptInterface(JsInterface(), "YoutubeJsInterface")

            val iframeConfig = iframeConfig("6t1k2W_sxtU", 0)

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    log("WebViewClient error", true)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    if (url.startsWith("y://error")) {
                        log("iFrame api script error", true)
                    }
                    return true
                }
            }

            val data = String.format(Locale.US, iframe!!, iframeConfig.toString())
            Log.d("YT", data)
            webView.loadDataWithBaseURL("https://www.youtube.com",
                    data, "text/html", "UTF-8", "https://youtube.com")

            fl.addView(myRl, 0,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 540))

            imv_play.setOnClickListener {
                playing = if (playing) {
                    pause()
                    imv_play.setImageResource(com.dastanapps.testcode.R.mipmap.ic_play)
                    hidePlayButton()
                    false
                } else {
                    play()
                    imv_play.setImageResource(com.dastanapps.testcode.R.mipmap.ic_pause)
                    hidePlayButton()
                    true
                }
            }

            fl.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        showPlayButton()
                        true
                    }
                }
                false
            }

            hidePlayButton()
        }


        fun play() {
            webView.loadUrl("javascript:(function() { player.playVideo(); })()")
        }

        fun pause() {
            webView.loadUrl("javascript:(function() { player.pauseVideo(); })()")
        }

        fun seekTo(seek: Int) {
            val stringBuilder = StringBuilder("javascript:(function() { player.seekTo(")
            stringBuilder.append(seek)
            stringBuilder.append(", true); })()")
            webView.loadUrl(stringBuilder.toString())
        }

        override fun onCancel(dialog: DialogInterface) {
            super.onCancel(dialog)
            myRl?.run {
                if (childCount > 0) {
                    removeAllViews();
                }
            }
            webView.removeJavascriptInterface("YoutubeJsInterface");
            webView.stopLoading()
            webView.destroy()
            handler.removeCallbacks(runnable)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.dastanapps.testcode.R.layout.activity_youtube_iframe)
        YTDialogFragment.getInstance(Bundle()).show(supportFragmentManager, "YT")
        btn_show.setOnClickListener {
            YTDialogFragment.getInstance(Bundle()).show(supportFragmentManager, "YT")
        }
    }
}
