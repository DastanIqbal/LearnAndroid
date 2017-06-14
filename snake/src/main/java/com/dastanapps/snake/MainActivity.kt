package com.dastanapps.snake

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceView


class MainActivity : AppCompatActivity() {
    private var screenWidth: Int = 640
    private var screenHeight: Int = 480

    //The portion of the bitmap to be drawn in the current frame
    private var rectToBeDrawn:Rect?=null
    //The dimensions of a single frame
    private var frameHeight:Int=64
    private var frameWidth:Int=64
    private var numFrames:Int=6
    private var frameNumber:Int=0

    private var hi: Int = 0
    private var fps: Int = 0
    private var lastTimeFrame: Long = 0

    private var canvas: Canvas? = null
    private var snakeView: SnakeAnimView? = null
    //The snake head sprite sheet
    private var headBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headBitmap = BitmapFactory.decodeResource(resources, R.drawable.head_sprite_sheet)
        snakeView = SnakeAnimView(this)
        setContentView(snakeView)

        val point: Point = Point()
        windowManager.defaultDisplay.getSize(point)
        screenWidth = point.x
        screenHeight = point.y

    }

    inner class SnakeAnimView(context: Context) : SurfaceView(context), Runnable {
        var thread: Thread? = null
        var paint: Paint = Paint()
        @Volatile
        var playingSnake: Boolean = false
        init{
            frameWidth= headBitmap?.width?.div(numFrames) as Int
            frameHeight=headBitmap?.height as Int
        }

        override fun run() {
            while (playingSnake) {
                updateScene()
                drawScene()
                controlToFPS()
            }
        }

        private fun updateScene() {
            //which frame should we draw
            rectToBeDrawn= Rect((frameNumber*frameWidth)-1,0,(frameNumber*frameWidth+frameWidth)-1,frameHeight)
            //now the next frame
            frameNumber++
            //don't try and draw frames that don't exist
            if(frameNumber == numFrames){
                frameNumber = 0 //back to the first frame
            }
        }

        private fun controlToFPS() {
            val timeThisFrame: Long = (System.currentTimeMillis() - lastTimeFrame)
            val timeToSleep = 500 - timeThisFrame
            if (timeThisFrame > 0) {
                fps = (1000 / timeThisFrame).toInt()
            }

            if (timeToSleep > 0) {
                Thread.sleep(timeToSleep)
            }
            lastTimeFrame = System.currentTimeMillis()
        }

        private fun drawScene() {
            if (holder.surface.isValid) {
                canvas = holder.lockCanvas()
                canvas?.drawColor(Color.RED)
                paint.color = Color.argb(255, 255, 255, 255)
                paint.textSize = 50f
                canvas?.drawText("Snake", 10f, 150f, paint)
                paint.textSize = 25f
                canvas?.drawText("Hi Score:" + hi, 160f, 150f, paint)

                //Draw the snake head
                //make this Rect whatever size and location you like
                //(startX, startY, endX, endY)
                val rect: Rect = Rect(screenWidth / 2 - 100, screenHeight / 2 - 100, screenWidth / 2 + 100, screenHeight / 2 + 100)
                canvas?.drawBitmap(headBitmap, rectToBeDrawn, rect, paint)
                holder.unlockCanvasAndPost(canvas)
            }
        }

        fun resume() {
            playingSnake = true
            thread = Thread(this)
            thread?.start()
        }

        fun pause() {
            playingSnake = false
            thread?.join()
        }
    }

    override fun onPause() {
        super.onPause()
        snakeView?.pause()
    }

    override fun onResume() {
        super.onResume()
        snakeView?.resume()
    }

    override fun onStop() {
        super.onStop()
        while (true) {
            snakeView?.pause()
            break
        }

        //finish();
    }

    override fun onBackPressed() {
        super.onBackPressed()
        snakeView?.pause()
        //  finish()
    }

}
