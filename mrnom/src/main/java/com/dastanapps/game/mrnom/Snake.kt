package com.dastanapps.game.mrnom

import java.util.*

class Snake {

    var parts: MutableList<SnakePart> = ArrayList()
    var direction: Int = 0

    init {
        direction = UP
        parts.add(SnakePart(5, 6))
        parts.add(SnakePart(5, 7))
        parts.add(SnakePart(5, 8))
    }

    fun turnLeft() {
        direction += 1
        if (direction > RIGHT)
            direction = UP
    }

    fun turnRight() {
        direction -= 1
        if (direction < UP)
            direction = RIGHT
    }

    fun eat() {
        val end = parts[parts.size - 1]
        parts.add(SnakePart(end.x, end.y))
    }

    fun advance() {
        val head = parts[0]

        val len = parts.size - 1
        for (i in len downTo 1) {
            val before = parts[i - 1]
            val part = parts[i]
            part.x = before.x
            part.y = before.y
        }

        if (direction == UP)
            head.y -= 1
        if (direction == LEFT)
            head.x -= 1
        if (direction == DOWN)
            head.y += 1
        if (direction == RIGHT)
            head.x += 1

        if (head.x < 0)
            head.x = 9
        if (head.x > 9)
            head.x = 0
        if (head.y < 0)
            head.y = 12
        if (head.y > 12)
            head.y = 0
    }

    fun checkBitten(): Boolean {
        val len = parts.size
        val head = parts[0]
        for (i in 1 until len) {
            val part = parts[i]
            if (part.x == head.x && part.y == head.y)
                return true
        }
        return false

    }

    companion object {
        val UP = 0
        val LEFT = 1
        val DOWN = 2
        val RIGHT = 3
    }
}

