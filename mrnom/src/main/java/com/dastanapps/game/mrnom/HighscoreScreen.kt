package com.dastanapps.game.mrnom

import com.dastanapps.gameframework.Game
import com.dastanapps.gameframework.Graphics
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Screen

class HighscoreScreen(gam: Game) : Screen(gam) {
    private var lines = arrayOfNulls<String>(5)

    init {

        for (i in 0..4) {
            lines[i] = "" + (i + 1) + ". " + Settings.highscores[i]
        }
    }

    override fun update(deltaTime: Float) {
        val touchEvents = game.getInput().getTouchEvents()
        game.getInput().getKeyEvents()

        val len = touchEvents.size
        (0 until len)
                .map { touchEvents[it] }
                .forEach {
                    when (it.type) {
                        Input.TouchEvent.TOUCH_UP -> {
                            if (it.x < 64 && it.y > 416) {
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
        g.drawPixmap(Assets.mainMenu!!, 64, 20, 0, 42, 196, 42)

        var y = 100
        for (i in 0..4) {
            drawText(g, lines[i].toString(), 20, y)
            y += 50
        }

        g.drawPixmap(Assets.buttons!!, 0, 416, 64, 64, 64, 64)
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

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}
}

