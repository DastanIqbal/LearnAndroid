package com.dastanapps.processing

import android.text.TextUtils
import java.io.File
import java.util.*

/**
 * Created by dastaniqbal on 19/07/2018.
 * dastanIqbal@marvelmedia.com
 * 19/07/2018 11:07
 */
class CmdlineBuilder {
    private val OVERWRITE_FLAG = "-y"
    private val INPUT_FILE_FLAG = "-i"
    private val STRICT_FLAG = "-strict"
    private val EXPERIMENTAL_FLAG = "-2"


    private val flags = ArrayList<String>()
    private val inputPaths = ArrayList<String>()
    private var outputPath: String? = null

    private var experimentalFlagSet: Boolean = true

    fun addInputPath(inputFilePath: String): CmdlineBuilder {
        val inputFile = File(inputFilePath)
        if (!inputFile.exists()) {
            throw RuntimeException("File provided by you does not exists")
        }

        inputPaths.add(inputFilePath)
        return this
    }

    fun outputPath(outputPath: String): CmdlineBuilder {
        if (TextUtils.isEmpty(outputPath)) {
            throw RuntimeException("It's not a good idea to pass empty path here")
        }

        this.outputPath = outputPath
        return this
    }

    fun customCommand(customCommand: String): CmdlineBuilder {
        if (TextUtils.isEmpty(customCommand)) {
            return this
        }

        val splitedCommand = customCommand.trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Collections.addAll(flags, *splitedCommand)
        return this
    }

    fun build(): Array<String> {
        checkInputPathsAndThrowIfEmpty()
        checkOutputPathAndThrowIfEmpty()

        val newFlags = ArrayList<String>()
        newFlags.add("ffmpeg")
        newFlags.add(OVERWRITE_FLAG)

        addInputPathsToFlags(newFlags)
        copyFlagsToNewDestination(newFlags)
        addExperimentalFlagIfNecessary(newFlags)
        newFlags.add(outputPath!!)

        return newFlags.toTypedArray()
    }

    private fun checkInputPathsAndThrowIfEmpty() {
        if (inputPaths.isEmpty()) {
            throw RuntimeException("You must specify at least one input path")
        }
    }

    private fun checkOutputPathAndThrowIfEmpty() {
        if (TextUtils.isEmpty(outputPath)) {
            throw RuntimeException("You must specify output path")
        }
    }

    private fun addInputPathsToFlags(flags: MutableList<String>) {
        for (path in inputPaths) {
            flags.add(INPUT_FILE_FLAG)
            flags.add(path)
        }
    }

    private fun copyFlagsToNewDestination(destination: MutableList<String>) {
        for (flag in flags) {
            destination.add(flag)
        }
    }

    private fun addExperimentalFlagIfNecessary(flags: MutableList<String>) {
        if (experimentalFlagSet) {
            flags.add(STRICT_FLAG)
            flags.add(EXPERIMENTAL_FLAG)
        }
    }
}