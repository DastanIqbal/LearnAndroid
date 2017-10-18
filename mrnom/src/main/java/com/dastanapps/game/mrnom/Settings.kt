package com.dastanapps.game.mrnom

import com.dastanapps.gameframework.FileIO
import java.io.*

object Settings {
    var soundEnabled = true
    var highscores = intArrayOf(100, 80, 50, 30, 10)
    fun load(files: FileIO) {
        var `in`: BufferedReader? = null
        try {
            `in` = BufferedReader(InputStreamReader(
                    files.readFile(".mrnom")))
            soundEnabled = java.lang.Boolean.parseBoolean(`in`.readLine())
            for (i in 0..4) {
                highscores[i] = Integer.parseInt(`in`.readLine())
            }
        } catch (e: IOException) {
            // :( It's ok we have defaults
        } catch (e: NumberFormatException) {
            // :/ It's ok, defaults save our day
        } finally {
            try {
                if (`in` != null)
                    `in`.close()
            } catch (e: IOException) {
            }

        }
    }

    fun save(files: FileIO) {
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(OutputStreamWriter(
                    files.writeFile(".mrnom")))
            out.write(java.lang.Boolean.toString(soundEnabled))
            for (i in 0..4) {
                out.write(Integer.toString(highscores[i]))
            }

        } catch (e: IOException) {
        } finally {
            try {
                if (out != null)
                    out.close()
            } catch (e: IOException) {
            }

        }
    }

    fun addScore(score: Int) {
        for (i in 0..4) {
            if (highscores[i] < score) {
                for (j in 4 downTo i + 1)
                    highscores[j] = highscores[j - 1]
                highscores[i] = score
                break
            }
        }
    }
}

