package com.dastanapps.gameframework.impl

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.dastanapps.gameframework.*

/**
 * Created by dastaniqbal on 14/10/2017.
 * dastanIqbal@marvelmedia.com
 * 14/10/2017 1:37
 */
class AndroidGame : Activity(), Game {
    override fun getStartScreen(): Screen {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    lateinit var renderView: AndroidFastRenderView
    lateinit var graphic: Graphics
    lateinit var input: AndroidInput
    lateinit var gameScreen: Screen

    val audio = AndroidAudio(this)
    val fileIO = AndroidFileIO(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val frameBufferWidth = if (isLandscape) 480 else 320
        val frameBufferHeight = if (isLandscape) 320 else 480
        val frameBuffer = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.RGB_565)

        val scaleX: Float = (frameBufferWidth / windowManager.defaultDisplay.width).toFloat()
        val scaleY: Float = (frameBufferHeight / windowManager.defaultDisplay.height).toFloat()

        renderView = AndroidFastRenderView(this, frameBuffer)
        graphic = AndroidGraphics(assets, frameBuffer)
        input = AndroidInput(this, renderView, scaleX, scaleY)
        gameScreen = getStartScreen()
        setContentView(renderView)
    }

    override fun onResume() {
        super.onResume()
        gameScreen.resume()
        renderView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameScreen.pause()
        renderView.pause()

        if (isFinishing) {
            gameScreen.dispose()
        }
    }

    override fun getInput(): Input {
        return input
    }

    override fun getFileIO(): FileIO {
        return fileIO
    }

    override fun getGraphics(): Graphics {
        return graphic
    }

    override fun getAudio(): Audio {
        return audio
    }

    override fun setScreen(screen: Screen) {
        if (screen == null)
            throw IllegalArgumentException("Screen must not be null")
        this.gameScreen.pause()
        this.gameScreen.dispose()
        screen.resume()
        screen.update(0f)
        this.gameScreen = screen
    }

    override fun getCurrentScreen(): Screen {
        return gameScreen
    }
}