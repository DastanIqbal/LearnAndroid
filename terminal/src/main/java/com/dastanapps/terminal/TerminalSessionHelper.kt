package com.dastanapps.terminal

import android.content.Context
import android.view.KeyEvent
import com.termux.terminal.TerminalColorScheme
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import java.io.File
import java.util.Properties

class TerminalSessionHelper(
    private val context: Context
) : TerminalSessionClient {

    private var session: TerminalSession? = null
    private var terminalView: TerminalView? = null
    private var colorScheme: TerminalColorScheme? = null

    companion object {
        const val FOREGROUND_COLOR = "#E1E1E6"
        const val BACKGROUND_COLOR = "#191622"
        const val FONT_SIZE = 40
        const val PROMPT_COLOR = "38;5;208" // ANSI 256-color orange
        const val CMD_COLOR = "0" // ANSI default foreground

        private const val ESC = ""

        fun buildPS1(): String {
            // Orange prompt with working directory, then reset to default color for typing
            return "\n${ESC}[${PROMPT_COLOR}m\$ ${ESC}[${CMD_COLOR}m"
        }

        fun buildColorProperties(): Properties = Properties().apply {
            setProperty("foreground", FOREGROUND_COLOR)
            setProperty("background", BACKGROUND_COLOR)
            setProperty("cursor", "#E1E1E6")
            setProperty("color0", "#201B2D")
            setProperty("color1", "#E96379")
            setProperty("color2", "#67E480")
            setProperty("color3", "#E7DE79")
            setProperty("color4", "#78D1E1")
            setProperty("color5", "#988BC7")
            setProperty("color6", "#A1EFE4")
            setProperty("color7", "#E1E1E6")
            setProperty("color8", "#4D4D4D")
            setProperty("color9", "#ED4556")
            setProperty("color10", "#00F769")
            setProperty("color11", "#E7DE79")
            setProperty("color12", "#78D1E1")
            setProperty("color13", "#988BC7")
            setProperty("color14", "#A4FFFF")
            setProperty("color15", "#F7F7FB")
        }
    }

    fun attachView(view: TerminalView) {
        terminalView = view
        // Apply colors once the view is attached
        applyColorsToView()
    }

    fun detachView() {
        terminalView = null
    }

    fun createSession(cols: Int = 80, rows: Int = 24): TerminalSession {
        val shellPath = findShell()
        val workingDir = context.filesDir.absolutePath.also {
            File(it).mkdirs()
        }

        val ps1 = buildPS1()

        val env = arrayOf(
            "HOME=${context.filesDir.absolutePath}",
            "PATH=/system/bin:/system/xbin:/sbin:/vendor/bin",
            "TERM=xterm-256color",
            "LANG=en_US.UTF-8",
            "PS1=$ps1"
        )

        colorScheme = TerminalColorScheme().apply {
            updateWith(buildColorProperties())
        }

        val newSession = TerminalSession(
            shellPath,
            workingDir,
            null,
            env,
            TerminalEmulatorHelper.TRANSCRIPT_ROWS,
            this
        )

        newSession.updateSize(cols, rows)
        session = newSession
        return newSession
    }

    fun applyColorsToView() {
        val view = terminalView ?: return
        val sess = session ?: return
        val scheme = colorScheme ?: return
        val emulator = sess.emulator ?: return

        System.arraycopy(
            scheme.mDefaultColors, 0,
            emulator.mColors.mCurrentColors, 0,
            com.termux.terminal.TextStyle.NUM_INDEXED_COLORS
        )
        view.onScreenUpdated()
    }

    fun sendCommand(command: String) {
        session?.write(command + "\r")
    }

    fun sendInput(input: String) {
        session?.write(input)
    }

    fun sendKeyDownUp(keyCode: Int) {
        val view = terminalView ?: return
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        view.onKeyDown(keyCode, keyEventDown)
        view.onKeyUp(keyCode, keyEventUp)
    }

    fun destroy() {
        session?.finishIfRunning()
        session = null
    }

    private fun findShell(): String {
        val candidates = listOf("/system/bin/sh", "/sbin/sh", "/vendor/bin/sh")
        return candidates.firstOrNull { File(it).exists() } ?: "/system/bin/sh"
    }

    // TerminalSessionClient callbacks

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView?.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {}

    override fun onSessionFinished(finishedSession: TerminalSession) {
        session = null
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {}

    override fun onPasteTextFromClipboard(session: TerminalSession?) {}

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(session: TerminalSession) {
        terminalView?.onScreenUpdated()
    }

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun getTerminalCursorStyle(): Int = 0

    override fun logError(tag: String?, msg: String?) {}
    override fun logWarn(tag: String?, msg: String?) {}
    override fun logInfo(tag: String?, msg: String?) {}
    override fun logDebug(tag: String?, msg: String?) {}
    override fun logVerbose(tag: String?, msg: String?) {}
    override fun logStackTraceWithMessage(tag: String?, msg: String?, e: Exception?) {}
    override fun logStackTrace(tag: String?, e: Exception?) {}
}