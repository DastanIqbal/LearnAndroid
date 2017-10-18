package com.dastanapps.game.mrnom


import com.dastanapps.gameframework.Game
import com.dastanapps.gameframework.Screen

class HelpScreen3(gam: Game) : Screen(gam) {

    override fun update(deltaTime: Float) {
        val touchEvents = game.getInput().getTouchEvents()
        game.getInput().getKeyEvents()


    }

    override fun present(deltaTime: Float) {
        val g = game.getGraphics()
        g.drawPixmap(Assets.background!!, 0f, 0f)
        g.drawPixmap(Assets.help3!!, 64f, 100f)
        g.drawPixmap(Assets.buttons!!, 256, 416, 0, 64, 64, 64)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }
}
