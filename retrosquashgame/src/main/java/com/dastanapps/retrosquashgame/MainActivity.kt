package com.dastanapps.retrosquashgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceView

class MainActivity : AppCompatActivity() {
    var screenWidth: Int = 640
    var screenHeight: Int = 480

    //ball attributes
    var ballWidth: Int = 30
    var ballPos: Point = Point()

    //racket attributes
    var racketWidth: Int = 20
    var racketHeight: Int = 10
    var racketPos: Point = Point()

    //racket movement
    var racketIsMovingLeft: Boolean = false
    var racketIsMovingRight: Boolean = false

    var lives: Int = 3
    var fps: Int = 25
    var score: Int = 0
    var lastFrameTime: Long = 0

    var canvas: Canvas? = null
    var sqashcourtview: SquashCourtView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sqashcourtview = SquashCourtView(this)
        setContentView(sqashcourtview)


        val point: Point = Point()
        windowManager.defaultDisplay.getSize(point);
        screenWidth = point.x
        screenHeight = point.y

        ballWidth = screenWidth / 35
        ballPos.x = screenWidth / 2
        ballPos.y = ballWidth + screenHeight / 10

        racketWidth = screenWidth / 8
        racketHeight = 10
        racketPos.x = screenWidth / 2
        racketPos.y = screenHeight - screenHeight / 8

        lives = 3
    }

    inner class SquashCourtView(context: Context) : SurfaceView(context), Runnable {
        var paint: Paint = Paint()
        var thread: Thread? = null

        @Volatile
        var playingSquash: Boolean = false

        override fun run() {
            while (playingSquash) {
                updateCourt()
                drawCourt()
                controlFPS()
            }
        }

        private fun updateCourt() {
            if (racketIsMovingLeft &&
                    racketPos.x > racketWidth / 2) {
                racketPos.x -= 10
            }
            if (racketIsMovingRight &&
                    racketPos.x < (screenWidth - racketWidth / 2)) {
                racketPos.x += 10
            }
        }

        private fun drawCourt() {
            if (holder.surface.isValid) {
                canvas = holder.lockCanvas()
                canvas?.drawColor(Color.RED)
                paint.color = Color.argb(255, 255, 255, 255)
                paint.textSize = 45f
                canvas?.drawText("Score: $score Lives:$lives fps:$fps", 20f, 40f, paint)

                //draw ball
                canvas?.drawRect(ballPos.x.toFloat(), ballPos.y.toFloat(), ballPos.x + ballWidth.toFloat(), ballPos.y + ballWidth.toFloat(), paint)

                //draw racket
                canvas?.drawRect((racketPos.x - (racketWidth / 2)).toFloat(), (racketPos.y - racketHeight / 2).toFloat(), (racketPos.x + racketWidth / 2).toFloat(), racketPos.y + racketHeight.toFloat(), paint)

                holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun controlFPS() {
            val timeThisFrame: Long = (System.currentTimeMillis() - lastFrameTime)
            val timeToSleep = 15 - timeThisFrame
            if (timeThisFrame > 0) {
                fps = (1000 / timeThisFrame).toInt()
            }
            if (timeToSleep > 0) Thread.sleep(timeToSleep)

            lastFrameTime = System.currentTimeMillis()
        }

        fun pause() {
            playingSquash = false
            thread?.join()
        }

        fun resume() {
            playingSquash = true
            thread = Thread(this)
            thread?.start()
        }

    }

    override fun onPause() {
        super.onPause()
        sqashcourtview?.pause()
    }

    override fun onResume() {
        super.onResume()
        sqashcourtview?.resume()
    }

    override fun onStop() {
        super.onStop()
        sqashcourtview?.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sqashcourtview?.pause()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x >= screenWidth / 2) {
                    racketIsMovingRight = true
                    racketIsMovingLeft = false
                } else {
                    racketIsMovingLeft = true
                    racketIsMovingRight = false
                }
            }
            MotionEvent.ACTION_UP -> {
                racketIsMovingLeft = false
                racketIsMovingRight = false
            }

        }
        return super.onTouchEvent(event)
    }
}
