package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.
 * dastanIqbal@marvelmedia.com
 * 21/06/2017 12:25
 */
abstract class Screen(game: Game) {
    abstract fun update(deltaTime: Float)
    abstract fun present(deltaTime: Float)
    abstract fun pause()
    abstract fun resume()
    abstract fun dispose()
}