package com.dastanapps.game.mrnom

import com.dastanapps.gameframework.Game
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Screen

/**
 * Created by dastaniqbal on 15/10/2017.
 * dastanIqbal@marvelmedia.com
 * 15/10/2017 2:02
 */
class MainMenuScreen(gam: Game) : Screen(gam) {
    override fun update(deltaTime: Float) {
        val g = game.getGraphics()
        val touchEvents = game.getInput().getTouchEvents()
        game.getInput().getKeyEvents()
        for (i in 0 until touchEvents.size) {
            val event = touchEvents[i]
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (inBounds(event, 0, g.getHeight() - 64, 64, 64)) {
                    Settings.soundEnabled = !Settings.soundEnabled
                    if (Settings.soundEnabled) {
                        Assets.click?.play(1f)
                    }
                }
                if (inBounds(event, 64, 220, 192, 42)) {
                    game.setScreen(GameScreen (game))
                    if (Settings.soundEnabled)
                        Assets.click?.play(1f)
                    return
                }
                if (inBounds(event, 64, 220 + 42, 192, 42)) {
                    game.setScreen(HighscoreScreen (game))
                    if (Settings.soundEnabled)
                        Assets.click?.play(1f)
                    return
                }
                if (inBounds(event, 64, 220 + 84, 192, 42)) {
                    game.setScreen(HelpScreen (game))
                    if (Settings.soundEnabled)
                        Assets.click?.play(1f)
                    return
                }
            }
        }
    }

    private fun inBounds(event: Input.TouchEvent, x: Int, y: Int, w: Int, h: Int): Boolean {
        return event.x > x && event.x < x + w - 1 && event.y > y && event.y < y + h - 1
    }

    override fun present(deltaTime: Float) {
        val g = game.getGraphics()
        g.drawPixmap(Assets.background!!, 0f, 0f)
        g.drawPixmap(Assets.logo!!, 32f, 20f)
        g.drawPixmap(Assets.mainMenu!!, 64f, 220f)
        if (Settings.soundEnabled) {
            g.drawPixmap(Assets.buttons!!, 0, 416, 0, 0, 64, 64)
        } else {
            g.drawPixmap(Assets.buttons!!, 0, 416, 64, 0, 64, 64)
        }
    }

    override fun pause() {
        Settings.save(game.getFileIO())
    }

    override fun resume() {}

    override fun dispose() {}
}