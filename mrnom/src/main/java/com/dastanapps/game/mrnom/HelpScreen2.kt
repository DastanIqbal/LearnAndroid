package com.dastanapps.game.mrnom


import com.dastanapps.gameframework.Game
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Screen

class HelpScreen2(gam: Game) : Screen(gam) {

    override fun update(deltaTime: Float) {
        val touchEvents = game.getInput().getTouchEvents()
        game.getInput().getKeyEvents()

        val len = touchEvents.size
        for (i in 0 until len) {
            val event = touchEvents.get(i)
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (event.x > 256 && event.y > 416) {
                    game.setScreen(HelpScreen3(game))
                    if (Settings.soundEnabled)
                        Assets.click!!.play(1f)
                    return
                }
            }
        }
    }

    override fun present(deltaTime: Float) {
        val g = game.getGraphics()
        g.drawPixmap(Assets.background!!, 0f, 0f)
        g.drawPixmap(Assets.help2!!, 64f, 100f)
        g.drawPixmap(Assets.buttons!!, 256, 416, 0, 64, 64, 64)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }
}
