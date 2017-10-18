package com.dastanapps.game.mrnom

import java.util.*

class World {

    var snake: Snake
    lateinit var stain: Stain
    var gameOver = false
    var score = 0

    internal var fields = Array(WORLD_WIDTH) { BooleanArray(WORLD_HEIGHT) }
    internal var random = Random()
    internal var tickTime = 0f
    internal var tick = TICK_INITIAL

    init {
        snake = Snake()
        placeStain()
    }

    private fun placeStain() {
        for (x in 0 until WORLD_WIDTH) {
            for (y in 0 until WORLD_HEIGHT) {
                fields[x][y] = false
            }
        }

        val len = snake.parts.size
        for (i in 0 until len) {
            val part = snake.parts[i]
            fields[part.x][part.y] = true
        }

        var stainX = random.nextInt(WORLD_WIDTH)
        var stainY = random.nextInt(WORLD_HEIGHT)
        while (true) {
            if (fields[stainX][stainY] == false)
                break
            stainX += 1
            if (stainX >= WORLD_WIDTH) {
                stainX = 0
                stainY += 1
                if (stainY >= WORLD_HEIGHT) {
                    stainY = 0
                }
            }
        }
        stain = Stain(stainX, stainY, random.nextInt(3))
    }

    fun update(deltaTime: Float) {
        if (gameOver)
            return

        tickTime += deltaTime

        while (tickTime > tick) {
            tickTime -= tick
            snake.advance()
            if (snake.checkBitten()) {
                gameOver = true
                return
            }

            val head = snake.parts[0]
            if (head.x == stain.x && head.y == stain.y) {
                score += SCORE_INCREMENT
                snake.eat()
                if (snake.parts.size == WORLD_WIDTH * WORLD_HEIGHT) {
                    gameOver = true
                    return
                } else {
                    placeStain()
                }

                if (score % 100 == 0 && tick - TICK_DECREMENT > 0) {
                    tick -= TICK_DECREMENT
                }
            }
        }
    }

    companion object {
        internal val WORLD_WIDTH = 10
        internal val WORLD_HEIGHT = 13
        internal val SCORE_INCREMENT = 10
        internal val TICK_INITIAL = 0.5f
        internal val TICK_DECREMENT = 0.05f
    }
}
