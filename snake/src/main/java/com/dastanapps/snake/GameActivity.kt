package com.dastanapps.snake

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import java.util.*


class GameActivity : AppCompatActivity() {
    var canvas: Canvas? = null
    var snakeView: SnakeView? = null

    var headBitmap: Bitmap? = null
    var bodyBitmap: Bitmap? = null
    var tailBitmap: Bitmap? = null
    var appleBitmap: Bitmap? = null

    var screenWidth: Int = 0
    var screenHeight: Int = 0

    private var topGap: Int = 0
    private var blockSize: Int = 0
    private var numBlocksWide: Int = 0
    private var numBlocksHigh: Int = 0

    private var fps: Int = 0
    private val hi: Int = 0
    private var score: Int = 0
    private var lastFrameTime: Long = 0

    //for snake movement
    var directionOfTravel = 0
    //0 = up, 1 = right, 2 = down, 3= left

    //Game objects
    var snakeX: IntArray = IntArray(200)
    var snakeY: IntArray = IntArray(200)
    var snakeLength: Int = 0
    var appleX: Int = 0
    var appleY: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureDisplay()
        snakeView = SnakeView(this)
        setContentView(snakeView)
    }

    inner class SnakeView(context: Context) : SurfaceView(context), Runnable {
        var thread: Thread? = null
        var paint: Paint = Paint()
        @Volatile var playingSnake: Boolean = false

        init {
            getSnake()
            getApple()
        }

        override fun run() {
            while (playingSnake) {
                updateGame()
                drawView()
                controlToFPS()
            }
        }

        private fun drawView() {
            if (holder.surface.isValid) {
                canvas = holder.lockCanvas()
                canvas?.drawColor(Color.BLACK)
                paint.color = Color.argb(255, 255, 255, 255)
                paint.textSize = topGap / 2f
                canvas?.drawText("Score:" + score + " Hi:" + hi, 10f, topGap - 6f, paint)

                // draw a border - 4 lines, top right, bottom , left
                paint.strokeWidth = 3f //4 pixel border
                canvas?.drawLine(1f, topGap.toFloat(), screenWidth - 1f, topGap.toFloat(), paint)
                canvas?.drawLine(screenWidth - 1f, topGap.toFloat(), screenWidth - 1f, (topGap + (numBlocksHigh * blockSize)).toFloat(), paint)
                canvas?.drawLine(screenWidth - 1f, topGap + (numBlocksHigh * blockSize).toFloat(), 1f, topGap + (numBlocksHigh * blockSize).toFloat(), paint)
                canvas?.drawLine(1f, topGap + (numBlocksHigh * blockSize).toFloat(), 1f, topGap.toFloat(), paint)

                //Draw the snake
                canvas?.drawBitmap(headBitmap, snakeX[0] * blockSize.toFloat(), (snakeY[0] * blockSize) + topGap.toFloat(), paint)

                //Draw the body
                for (i in 1..snakeLength - 1 - 1) {
                    canvas?.drawBitmap(bodyBitmap, (snakeX[i] * blockSize).toFloat(), (snakeY[i] * blockSize + topGap).toFloat(), paint)
                }
                //draw the tail
                canvas?.drawBitmap(tailBitmap, snakeX[snakeLength - 1] * blockSize.toFloat(), (snakeY[snakeLength - 1] * blockSize) + topGap.toFloat(), paint)

                //draw the apple
                canvas?.drawBitmap(appleBitmap, appleX * blockSize.toFloat(), (appleY * blockSize) + topGap.toFloat(), paint);

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

        fun getSnake() {
            snakeLength = 3
            //start snake head in the middle of screen
            snakeX[0] = numBlocksWide / 2
            snakeY[0] = numBlocksHigh / 2

            //Then the body
            snakeX[1] = snakeX[0] - 1
            snakeY[1] = snakeY[0]

            //And the tail
            snakeX[2] = snakeX[1] - 1
            snakeY[2] = snakeY[0]
        }

        fun getApple() {
            val random = Random()
            appleX = random.nextInt(numBlocksWide - 1) + 1
            appleY = random.nextInt(numBlocksHigh - 1) + 1
        }

        fun updateGame() {

            //Did the player get the apple
            if (snakeX[0] == appleX && snakeY[0] == appleY) {
                //grow the snake
                snakeLength++
                //replace the apple
                getApple()
                //add to the score
                score = score + snakeLength
            }

            //move the body - starting at the back
            for (i in snakeLength downTo 1) {
                snakeX[i] = snakeX[i - 1]
                snakeY[i] = snakeY[i - 1]
            }

            //Move the head in the appropriate direction
            when (directionOfTravel) {
                0//up
                -> snakeY[0]--

                1//right
                -> snakeX[0]++

                2//down
                -> snakeY[0]++

                3//left
                -> snakeX[0]--
            }

            //Have we had an accident
            var dead = false
            //with a wall
            if (snakeX[0] == -1) dead = true
            if (snakeX[0] >= numBlocksWide) dead = true
            if (snakeY[0] == -1) dead = true
            if (snakeY[0] == numBlocksHigh) dead = true
            //or eaten ourselves?
            for (i in snakeLength - 1 downTo 1) {
                if (i > 4 && snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                    dead = true
                }
            }


            if (dead) {
                //start again
                // soundPool.play(sample4, 1, 1, 0, 0, 1)
                score = 0
                getSnake()

            }
        }

        override fun onTouchEvent(motionEvent: MotionEvent): Boolean {

            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> if (motionEvent.x >= screenWidth / 2) {
                    //turn right
                    directionOfTravel++
                    if (directionOfTravel == 4) {//no such direction
                        //loop back to 0(up)
                        directionOfTravel = 0
                    }
                } else {
                    //turn left
                    directionOfTravel--
                    if (directionOfTravel == -1) {//no such direction
                        //loop back to 0(up)
                        directionOfTravel = 3
                    }
                }
            }
            return true
        }

    }

    override fun onResume() {
        super.onResume()
        snakeView?.resume()
    }

    override fun onPause() {
        super.onPause()
        snakeView?.pause()
    }

    override fun onStop() {
        super.onStop()
        snakeView?.pause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun controlToFPS() {
        val timeThisFrame = System.currentTimeMillis() - lastFrameTime
        val timeToSleep = 500 - timeThisFrame
        if (timeThisFrame > 0) {
            fps = (1000 / timeThisFrame).toInt()
        }
        if (timeToSleep > 0) {
            Thread.sleep(timeToSleep)
        }
        lastFrameTime = System.currentTimeMillis()
    }

    private fun configureDisplay() {
        //find out the width and height of the screen
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        screenWidth = point.x
        screenHeight = point.y

        topGap = screenHeight / 14

        //Determine the size of each block/place on the game board
        blockSize = screenWidth / 40

        //Determine how many game blocks will fit into the height and width
        //Leave one block for the score at the top
        numBlocksWide = 40
        numBlocksHigh = (screenHeight - topGap) / blockSize

        //Load and scale bitmaps
        headBitmap = BitmapFactory.decodeResource(resources, R.drawable.head)
        bodyBitmap = BitmapFactory.decodeResource(resources, R.drawable.body)
        tailBitmap = BitmapFactory.decodeResource(resources, R.drawable.tail)
        appleBitmap = BitmapFactory.decodeResource(resources, R.drawable.apple)

        //scale the bitmaps to match the block size
        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false)
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false)
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize, false)
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false)

    }
}
