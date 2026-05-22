package com.dastanapps.terminal

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color as ComposeColor
import com.termux.terminal.PtyHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.connectbot.terminal.ModifierManager
import org.connectbot.terminal.TerminalEmulator
import org.connectbot.terminal.TerminalEmulatorFactory
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class ConnectBotSessionHelper(private val context: Context) {

    var emulator: TerminalEmulator? = null
        private set

    private var ptyFd: FileDescriptor? = null
    private var ptyInput: FileOutputStream? = null
    private var ptyOutput: FileInputStream? = null
    private var processId: Int = -1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var readerJob: kotlinx.coroutines.Job? = null
    private var waitJob: kotlinx.coroutines.Job? = null

    private var currentScheme: TerminalColorScheme = TerminalColorScheme.Dracula

    // Locked (sticky) modifier state set by UI toggle buttons.
    // Using MutableState so Compose observes changes and recomposes the buttons.
    val ctrlState = mutableStateOf(false)
    val altState = mutableStateOf(false)
    val shiftState = mutableStateOf(false)

    val modifierManager = object : ModifierManager {
        override fun isCtrlActive() = ctrlState.value
        override fun isAltActive() = altState.value
        override fun isShiftActive() = shiftState.value
        override fun clearTransients() {
            // We only use locked (sticky) modifiers from the UI buttons.
            // Transient modifiers are not used, so nothing to clear.
        }
    }

    fun createEmulator(): TerminalEmulator {
        val em = TerminalEmulatorFactory.create(
            initialRows = 24,
            initialCols = 80,
            defaultForeground = ComposeColor(AndroidColor.parseColor(currentScheme.foreground)),
            defaultBackground = ComposeColor(AndroidColor.parseColor(currentScheme.background)),
            onKeyboardInput = { data ->
                ptyInput?.write(data)
                ptyInput?.flush()
            },
            onResize = { dims ->
                resizePty(dims.rows, dims.columns)
            },
            onBell = { /* could vibrate */ },
            autoDetectUrls = false,
            boldAsBright = true
        )

        // Apply custom ANSI color palette
        applyScheme(em, currentScheme)

        emulator = em
        return em
    }

    fun applyColorScheme(scheme: TerminalColorScheme) {
        currentScheme = scheme
        emulator?.let { applyScheme(it, scheme) }
    }

    private fun applyScheme(em: TerminalEmulator, scheme: TerminalColorScheme) {
        em.applyColorScheme(
            ansiColors = scheme.ansiColors,
            defaultForeground = AndroidColor.parseColor(scheme.foreground),
            defaultBackground = AndroidColor.parseColor(scheme.background)
        )
    }

    fun startShell() {
        val em = emulator ?: return

        try {
            val workingDir = "/sdcard"
            File(workingDir).mkdirs()

            val ps1 = buildPs1()

            val env = arrayOf(
                "HOME=$workingDir",
                "PATH=/system/bin:/system/xbin:/sbin:/vendor/bin",
                "TERM=xterm-256color",
                "LANG=en_US.UTF-8",
                "PS1=$ps1"
            )

            val pids = IntArray(1)
            val fd = PtyHelper.createSubprocess(
                cmd = "/system/bin/sh",
                cwd = workingDir,
                args = null,
                envVars = env,
                processId = pids,
                rows = 24,
                columns = 80
            )
            processId = pids[0]
            ptyFd = fd
            ptyOutput = FileInputStream(fd)
            ptyInput = FileOutputStream(fd)

            // Ensure cursor is visible (DEC Private Mode Set - show cursor)
            ptyInput?.write("[?25h".toByteArray())
            // Force shell to print prompt by sending a no-op command
            ptyInput?.write("pwd\n".toByteArray())
            ptyInput?.flush()

            // Read PTY output on a background thread and feed to emulator
            readerJob = scope.launch {
                val buffer = ByteArray(4096)
                val stream = ptyOutput ?: return@launch
                while (isActive) {
                    val bytesRead = stream.read(buffer)
                    if (bytesRead == -1) break
                    em.writeInput(buffer, 0, bytesRead)
                }
            }

            // Wait for process exit
            waitJob = scope.launch {
                PtyHelper.waitFor(processId)
            }
        } catch (e: Exception) {
            em.writeInput("Failed to start shell: ${e.message}\r\n".toByteArray())
        }
    }

    fun updatePrompt() {
        val ps1 = buildPs1()
        val cmd = "export PS1='$ps1'\n"
        ptyInput?.write(cmd.toByteArray())
        ptyInput?.flush()
    }

    private fun buildPs1(): String {
        // 39m = default foreground (matches current theme)
        // 0m  = reset attributes
        return "\n[39m\$ [0m"
    }

    fun resizePty(rows: Int, cols: Int) {
        val fd = ptyFd ?: return
        try {
            PtyHelper.setPtyWindowSize(fd, rows, cols)
        } catch (e: Exception) {
            // Ignore resize errors
        }
    }

    fun sendInput(input: String) {
        ptyInput?.write(input.toByteArray())
        ptyInput?.flush()
    }

    fun toggleCtrl(): Boolean {
        ctrlState.value = !ctrlState.value
        return ctrlState.value
    }

    fun toggleAlt(): Boolean {
        altState.value = !altState.value
        return altState.value
    }

    fun toggleShift(): Boolean {
        shiftState.value = !shiftState.value
        return shiftState.value
    }

    fun isCtrlActive() = ctrlState.value
    fun isAltActive() = altState.value
    fun isShiftActive() = shiftState.value

    fun destroy() {
        readerJob?.cancel()
        waitJob?.cancel()
        scope.cancel()
        try {
            ptyFd?.let { PtyHelper.close(it) }
        } catch (_: Exception) {}
        ptyInput?.close()
        ptyOutput?.close()
        ptyFd = null
        emulator = null
    }

    companion object {
        const val PROMPT_COLOR = "38;5;208"
        const val CMD_COLOR = "0"
    }
}