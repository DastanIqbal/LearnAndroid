package com.dastanapps.game.mrnom

import com.dastanapps.gameframework.Game
import com.dastanapps.gameframework.Graphics.PixmapFormat
import com.dastanapps.gameframework.Screen



/**
 * Created by dastaniqbal on 15/10/2017.

 * 15/10/2017 1:59
 */
class LoadingScreen(gam: Game) : Screen(gam) {
    override fun update(deltaTime: Float) {
        val g = game.getGraphics()
        Assets.background = g.newPixmap("background.png", PixmapFormat.RGB565)
        Assets.logo = g.newPixmap("logo.png", PixmapFormat.ARGB4444)
        Assets.mainMenu = g.newPixmap("mainmenu.png", PixmapFormat.ARGB4444)
        Assets.buttons = g.newPixmap("buttons.png", PixmapFormat.ARGB4444)
        Assets.help1 = g.newPixmap("help1.png", PixmapFormat.ARGB4444)
        Assets.help2 = g.newPixmap("help2.png", PixmapFormat.ARGB4444)
        Assets.help3 = g.newPixmap("help3.png", PixmapFormat.ARGB4444)
        Assets.numbers = g.newPixmap("numbers.png", PixmapFormat.ARGB4444)
        Assets.ready = g.newPixmap("ready.png", PixmapFormat.ARGB4444)
        Assets.pause = g.newPixmap("pausemenu.png", PixmapFormat.ARGB4444)
        Assets.gameOver = g.newPixmap("gameover.png", PixmapFormat.ARGB4444)
        Assets.headUp = g.newPixmap("headup.png", PixmapFormat.ARGB4444)
        Assets.headLeft = g.newPixmap("headleft.png", PixmapFormat.ARGB4444)
        Assets.headDown = g.newPixmap("headdown.png", PixmapFormat.ARGB4444)
        Assets.headRight = g.newPixmap("headright.png", PixmapFormat.ARGB4444)
        Assets.tail = g.newPixmap("tail.png", PixmapFormat.ARGB4444)
        Assets.stain1 = g.newPixmap("stain1.png", PixmapFormat.ARGB4444)
        Assets.stain2 = g.newPixmap("stain2.png", PixmapFormat.ARGB4444)
        Assets.stain3 = g.newPixmap("stain3.png", PixmapFormat.ARGB4444)
        Assets.click = game.getAudio().newSound("click.ogg")
        Assets.eat = game.getAudio().newSound("eat.ogg")
        Assets.bitten = game.getAudio().newSound("bitten.ogg")
        Settings.load(game.getFileIO())
        game.setScreen(MainMenuScreen(game))
    }

    override fun present(deltaTime: Float) {}

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}
}