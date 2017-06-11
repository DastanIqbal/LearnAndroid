package com.dastanapps.retrosquashgame

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceView
import java.util.*


class MainActivity : AppCompatActivity() {
    var screenWidth: Int = 640
    var screenHeight: Int = 480

    //ball attributes
    var ballWidth: Int = 30
    var ballPos: Point = Point()

    var ballIsMovingRight: Boolean = false
    var ballIsMovingLeft: Boolean = false
    var ballIsMovingUp: Boolean = false
    var ballIsMovingDown: Boolean = false

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

    //Sound
    //initialize sound variables
    private var soundPool: SoundPool? = null
    var sample1 = -1
    var sample2 = -1
    var sample3 = -1
    var sample4 = -1

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
        racketPos.y = screenHeight - screenHeight / 5

        lives = 3

        //Sound code
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        var descriptor: AssetFileDescriptor = assets.openFd("sample1.ogg")

        //create our three fx in memory ready for use
        sample1 = soundPool!!.load(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length, 0)

        descriptor = assets.openFd("sample2.ogg")
        sample2 = soundPool!!.load(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length, 0)


        descriptor = assets.openFd("sample3.ogg")
        sample3 = soundPool!!.load(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length, 0)

        descriptor = assets.openFd("sample4.ogg")
        sample4 = soundPool!!.load(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length, 0)

    }

    inner class SquashCourtView(context: Context) : SurfaceView(context), Runnable {
        var paint: Paint = Paint()
        var thread: Thread? = null
        @Volatile
        var playingSquash: Boolean = false

        init {
            ballIsMovingDown = true
            handleBallDirection()
        }

        private fun handleBallDirection() {
            val randomNumber: Random = Random()
            val ballDirection: Int = randomNumber.nextInt(3)
            when (ballDirection) {
                0 -> {
                    ballIsMovingLeft = true
                    ballIsMovingRight = false
                }
                1 -> {
                    ballIsMovingLeft = false
                    ballIsMovingRight = true
                }
                2 -> {
                    ballIsMovingLeft = false
                    ballIsMovingRight = false
                }
            }
        }

        override fun run() {
            while (playingSquash) {
                updateCourt()
                drawCourt()
                controlFPS()
            }
        }

        private fun updateCourt() {
            //Move Racket
            if (racketIsMovingLeft &&
                    racketPos.x > racketWidth / 2) {
                racketPos.x -= 10
            }
            if (racketIsMovingRight &&
                    racketPos.x < (screenWidth - racketWidth / 2)) {
                racketPos.x += 10
            }

            //Detect Collision
            //Right
            if (ballPos.x + ballWidth > screenWidth) {
                ballIsMovingLeft = true
                ballIsMovingRight = false
                soundPool?.play(sample1,1f,1f,0,0,1f)
            }

            //Left
            if (ballPos.x < 0) {
                ballIsMovingLeft = false
                ballIsMovingRight = true
                soundPool?.play(sample1,1f,1f,0,0,1f)
            }

            //Top
            if (ballPos.y <= 0) {
                ballIsMovingDown = true
                ballIsMovingUp = false
                ballPos.y = 1
                soundPool?.play(sample2,1f,1f,0,0,1f)
            }

            //Bottom
            if (ballPos.y > screenHeight - ballWidth) {
                lives -= 1
                if (lives == 0) {
                    lives = 3
                    score = 0
                    soundPool?.play(sample4,1f,1f,0,0,1f)
                }
                ballPos.y = 1 + ballWidth
                val randomNumber: Random = Random()
                val startX: Int = randomNumber.nextInt(screenWidth - ballWidth) + 1
                ballPos.x = startX + ballWidth
                handleBallDirection()
            }

            //Handle ballDirection
            if (ballIsMovingDown) ballPos.y += 6
            if (ballIsMovingUp) ballPos.y -= 10
            if (ballIsMovingLeft) ballPos.x -= 12
            if (ballIsMovingRight) ballPos.x += 12

            //Detect Racket Collision
            if (ballPos.y + ballWidth >= (racketPos.y - racketHeight / 2)
                    && ballPos.y - ballWidth <= (racketPos.y + racketHeight / 2)) {
                val halfRacket: Int = racketWidth / 2
                if (ballPos.x + ballWidth > (racketPos.x - halfRacket) &&
                        ballPos.x - ballWidth < (racketPos.x + halfRacket)) {
                    soundPool?.play(sample3,1f,1f,0,0,1f)
                    score++
                    ballIsMovingUp = true
                    ballIsMovingDown = false
                    if (ballPos.x > racketPos.x) {
                        ballIsMovingRight = true
                        ballIsMovingLeft = false
                    } else {
                        ballIsMovingLeft = true
                        ballIsMovingRight = false
                    }
                }
            }

        }

        private fun drawCourt() {
            if (holder.surface.isValid) {
                canvas = holder.lockCanvas()
                canvas?.drawColor(Color.BLACK)
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
