package com.dastanapps.testframework.test

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager

/**
 * Created by dastaniqbal on 26/07/2017.

 * 26/07/2017 6:48
 */

class SurfaceViewTest : AppCompatActivity() {
    lateinit var fastRenderView: FastRenderView
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        fastRenderView = FastRenderView(this)
        setContentView(fastRenderView)
    }

    override fun onResume() {
        super.onResume()
        fastRenderView.resume()
    }

    override fun onPause() {
        super.onPause()
        fastRenderView.pause()
    }

    inner class FastRenderView(context: Context) : SurfaceView(context), Runnable {
        var running = false
        lateinit var thread: Thread

        override fun run() {
            while (running) {
                if (!holder.surface.isValid) continue

                val canvas = holder.lockCanvas()
                canvas.drawRGB(500, 0, 0)
                holder.unlockCanvasAndPost(canvas)
            }
        }

        fun resume() {
            running = true
            thread = Thread(this)
            thread.start()
        }

        fun pause() {
            running = false
            thread.join()
        }

    }
}
