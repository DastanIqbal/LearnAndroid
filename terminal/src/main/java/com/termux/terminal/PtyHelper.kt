package com.termux.terminal

import java.io.FileDescriptor

/**
 * Public wrapper around the package-private JNI class for PTY creation.
 * Allows external packages to create PTY-based subprocesses using Termux's native library.
 */
object PtyHelper {

    init {
        // Ensure the native library is loaded
        try {
            System.loadLibrary("termux")
        } catch (e: UnsatisfiedLinkError) {
            // Already loaded by another classloader
        }
    }

    /**
     * Create a PTY subprocess.
     * @return FileDescriptor for the master PTY side
     */
    @JvmStatic
    fun createSubprocess(
        cmd: String,
        cwd: String?,
        args: Array<String>?,
        envVars: Array<String>?,
        processId: IntArray,
        rows: Int,
        columns: Int
    ): FileDescriptor {
        val fd = JNI.createSubprocess(cmd, cwd, args, envVars, processId, rows, columns)
        val fileDescriptor = FileDescriptor()
        try {
            val field = FileDescriptor::class.java.getDeclaredField("descriptor")
            field.isAccessible = true
            field.setInt(fileDescriptor, fd)
        } catch (e: Exception) {
            // Fallback for Android API where field name differs
            try {
                val field = FileDescriptor::class.java.getDeclaredField("fd")
                field.isAccessible = true
                field.setInt(fileDescriptor, fd)
            } catch (e2: Exception) {
                throw RuntimeException("Unable to set FileDescriptor fd", e2)
            }
        }
        return fileDescriptor
    }

    @JvmStatic
    fun setPtyWindowSize(fd: FileDescriptor, rows: Int, cols: Int) {
        val rawFd = getRawFd(fd)
        JNI.setPtyWindowSize(rawFd, rows, cols)
    }

    @JvmStatic
    fun waitFor(processId: Int): Int {
        return JNI.waitFor(processId)
    }

    @JvmStatic
    fun close(fd: FileDescriptor) {
        val rawFd = getRawFd(fd)
        JNI.close(rawFd)
    }

    private fun getRawFd(fd: FileDescriptor): Int {
        return try {
            val field = FileDescriptor::class.java.getDeclaredField("descriptor")
            field.isAccessible = true
            field.getInt(fd)
        } catch (e: Exception) {
            val field = FileDescriptor::class.java.getDeclaredField("fd")
            field.isAccessible = true
            field.getInt(fd)
        }
    }
}