package com.dastanapps.gameframework.impl

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.SurfaceView

/**
 * Created by dastaniqbal on 14/10/2017.
 * dastanIqbal@marvelmedia.com
 * 14/10/2017 1:38
 */
class AndroidFastRenderView(val game: AndroidGame,val frameBuffer:Bitmap) : SurfaceView(game), Runnable {
    var running = false
    var thread:Thread? = null

    fun resume() {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause(){
        running=false
        thread?.join()
    }

    override fun run() {
        val destRect=Rect()
        var startTime=System.nanoTime()
        while(running){
            if(holder?.surface?.isValid!!)
                continue
            val deltaTime=(System.nanoTime()-startTime)/1000000000f
            startTime=System.nanoTime()

            val canvas=holder.lockCanvas()
            canvas.getClipBounds(destRect)
            canvas.drawBitmap(frameBuffer,null,destRect,null)
            holder.unlockCanvasAndPost(canvas)
        }
    }
}