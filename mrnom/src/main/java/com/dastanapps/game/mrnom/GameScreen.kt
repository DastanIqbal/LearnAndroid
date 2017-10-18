package com.dastanapps.game.mrnom

import android.graphics.Color
import com.dastanapps.gameframework.*

class GameScreen(gam: Game) : Screen(gam) {

    private var state = GameState.Ready
    private var world: World = World()
    private var oldScore = 0
    private var score = "0"

    internal enum class GameState {
        Ready,
        Running,
        Paused,
        GameOver
    }

    override fun update(deltaTime: Float) {
        val touchEvents = game.getInput().getTouchEvents()
        game.getInput().getKeyEvents()

        if (state == GameState.Ready)
            updateReady(touchEvents)
        if (state == GameState.Running)
            updateRunning(touchEvents, deltaTime)
        if (state == GameState.Paused)
            updatePaused(touchEvents)
        if (state == GameState.GameOver)
            updateGameOver(touchEvents)
    }

    private fun updateReady(touchEvents: List<Input.TouchEvent>) {
        if (touchEvents.isNotEmpty())
            state = GameState.Running
    }

    private fun updateRunning(touchEvents: List<Input.TouchEvent>, deltaTime: Float) {
        val len = touchEvents.size
        for (i in 0 until len) {
            val event = touchEvents[i]
            when (event.type) {
                Input.TouchEvent.TOUCH_UP -> {
                    if (event.x < 64 && event.y < 64) {
                        if (Settings.soundEnabled)
                            Assets.click!!.play(1f)
                        state = GameState.Paused
                        return
                    }
                }
                Input.TouchEvent.TOUCH_DOWN -> {
                    if (event.x < 64 && event.y > 416) {
                        world.snake.turnLeft()
                    }
                    if (event.x > 256 && event.y > 416) {
                        world.snake.turnRight()
                    }
                }
            }

            world.update(deltaTime)
            if (world.gameOver) {
                if (Settings.soundEnabled)
                    Assets.bitten!!.play(1f)
                state = GameState.GameOver
            }
            if (oldScore != world.score) {
                oldScore = world.score
                score = "" + oldScore
                if (Settings.soundEnabled)
                    Assets.eat!!.play(1f)
            }
        }
    }

    private fun updatePaused(touchEvents: List<Input.TouchEvent>) {
        val len = touchEvents.size
        (0 until len)
                .map { touchEvents[it] }
                .forEach {
                    when (it.type) {
                        Input.TouchEvent.TOUCH_UP -> {
                            if (it.x in 81..240) {
                                if (it.y in 101..148) {
                                    if (Settings.soundEnabled)
                                        Assets.click!!.play(1f)
                                    state = GameState.Running
                                    return
                                }
                                if (it.y in 149..195) {
                                    if (Settings.soundEnabled)
                                        Assets.click!!.play(1f)
                                    game.setScreen(MainMenuScreen(game))
                                    return
                                }
                            }
                        }
                    }
                }
    }

    private fun updateGameOver(touchEvents: List<Input.TouchEvent>) {
        val len = touchEvents.size
        (0 until len)
                .map { touchEvents[it] }
                .forEach {
                    when (it.type) {
                        Input.TouchEvent.TOUCH_UP -> {
                            if (it.x in 128..192 &&
                                    it.y >= 200 && it.y <= 264) {
                                if (Settings.soundEnabled)
                                    Assets.click!!.play(1f)
                                game.setScreen(MainMenuScreen(game))
                                return
                            }
                        }
                    }
                }
    }

    override fun present(deltaTime: Float) {
        val g = game.getGraphics()

        g.drawPixmap(Assets.background!!, 0f, 0f)
        drawWorld(world)
        if (state == GameState.Ready)
            drawReadyUI()
        if (state == GameState.Running)
            drawRunningUI()
        if (state == GameState.Paused)
            drawPausedUI()
        if (state == GameState.GameOver)
            drawGameOverUI()

        drawText(g, score, g.getWidth() / 2 - score.length * 20 / 2, g.getHeight() - 42)
    }

    private fun drawWorld(world: World) {
        val g = game.getGraphics()
        val snake = world.snake
        val head = snake.parts[0]
        val stain = world.stain


        var stainPixmap: Pixmap? = null
        if (stain.type == Stain.TYPE_1)
            stainPixmap = Assets.stain1
        if (stain.type == Stain.TYPE_2)
            stainPixmap = Assets.stain2
        if (stain.type == Stain.TYPE_3)
            stainPixmap = Assets.stain3
        var x = stain.x * 32
        var y = stain.y * 32
        g.drawPixmap(stainPixmap!!, x.toFloat(), y.toFloat())

        val len = snake.parts.size
        for (i in 1 until len) {
            val part = snake.parts[i]
            x = part.x * 32
            y = part.y * 32
            g.drawPixmap(Assets.tail!!, x.toFloat(), y.toFloat())
        }

        var headPixmap: Pixmap? = null
        if (snake.direction == Snake.UP)
            headPixmap = Assets.headUp
        if (snake.direction == Snake.LEFT)
            headPixmap = Assets.headLeft
        if (snake.direction == Snake.DOWN)
            headPixmap = Assets.headDown
        if (snake.direction == Snake.RIGHT)
            headPixmap = Assets.headRight
        x = head.x * 32 + 16
        y = head.y * 32 + 16
        g.drawPixmap(headPixmap!!, (x - headPixmap.getWidth() / 2).toFloat(), (y - headPixmap.getHeight() / 2).toFloat())
    }

    private fun drawReadyUI() {
        val g = game.getGraphics()

        g.drawPixmap(Assets.ready!!, 47f, 100f)
        g.drawLine(0f, 416f, 480f, 416f, Color.BLACK)
    }

    private fun drawRunningUI() {
        val g = game.getGraphics()

        g.drawPixmap(Assets.buttons!!, 0, 0, 64, 128, 64, 64)
        g.drawLine(0f, 416f, 480f, 416f, Color.BLACK)
        g.drawPixmap(Assets.buttons!!, 0, 416, 64, 64, 64, 64)
        g.drawPixmap(Assets.buttons!!, 256, 416, 0, 64, 64, 64)
    }

    private fun drawPausedUI() {
        val g = game.getGraphics()

        g.drawPixmap(Assets.pause!!, 80f, 100f)
        g.drawLine(0f, 416f, 480f, 416f, Color.BLACK)
    }

    private fun drawGameOverUI() {
        val g = game.getGraphics()

        g.drawPixmap(Assets.gameOver!!, 62f, 100f)
        g.drawPixmap(Assets.buttons!!, 128, 200, 0, 128, 64, 64)
        g.drawLine(0f, 416f, 480f, 416f, Color.BLACK)
    }

    private fun drawText(g: Graphics, line: String, x: Int, y: Int) {
        var x = x
        val len = line.length
        for (i in 0 until len) {
            val character = line[i]

            if (character == ' ') {
                x += 20
                continue
            }

            var srcX = 0
            var srcWidth = 0
            if (character == '.') {
                srcX = 200
                srcWidth = 10
            } else {
                srcX = (character - '0') * 20
                srcWidth = 20
            }

            g.drawPixmap(Assets.numbers!!, x, y, srcX, 0, srcWidth, 32)
            x += srcWidth
        }
    }

    override fun pause() {
        if (state == GameState.Running)
            state = GameState.Paused

        if (world.gameOver) {
            Settings.addScore(world.score)
            Settings.save(game.getFileIO())
        }
    }

    override fun resume() {

    }

    override fun dispose() {

    }
}

