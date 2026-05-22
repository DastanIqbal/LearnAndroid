package com.dastanapps.terminal

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen() {
    var selectedEngine by remember { mutableStateOf(TerminalEngine.TERMUX) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal") },
                actions = {
                    EnginePicker(
                        selectedEngine = selectedEngine,
                        onEngineSelected = { selectedEngine = it }
                    )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        when (selectedEngine) {
            TerminalEngine.TERMUX -> TermuxTerminalContent(modifier = Modifier.padding(innerPadding))
            TerminalEngine.CONNECTBOT -> ConnectBotTerminalContent()
        }
    }
}

@Composable
private fun EnginePicker(
    selectedEngine: TerminalEngine,
    onEngineSelected: (TerminalEngine) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TerminalEngine.entries.forEach { engine ->
            val isSelected = engine == selectedEngine
            Button(
                onClick = { onEngineSelected(engine) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFE96379) else Color(0xFF2A2540),
                    contentColor = Color.White
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(engine.label, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TermuxTerminalContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessionHelper = remember { TerminalSessionHelper(context) }
    val terminalSession = remember { mutableStateOf<TerminalSession?>(null) }
    val terminalViewRef = remember { mutableStateOf<TerminalView?>(null) }
    val ctrlActive = remember { mutableStateOf(false) }
    val altActive = remember { mutableStateOf(false) }
    val fnActive = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val session = sessionHelper.createSession()
        terminalSession.value = session
        terminalViewRef.value?.let { view ->
            view.attachSession(session)
            sessionHelper.attachView(view)
        }
        onDispose {
            sessionHelper.detachView()
            sessionHelper.destroy()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                TerminalView(ctx, null).apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    setTerminalViewClient(TerminalClient(ctrlActive, altActive, fnActive, sessionHelper))
                    setTextSize(TerminalSessionHelper.FONT_SIZE)
                    setBackgroundColor(parseColor(TerminalSessionHelper.BACKGROUND_COLOR))
                    terminalSession.value?.let { attachSession(it) }
                    sessionHelper.attachView(this)
                    setKeepScreenOn(true)
                    terminalViewRef.value = this
                    setOnClickListener {
                        requestFocus()
                        val imm = ctx.getSystemService(InputMethodManager::class.java)
                        imm?.showSoftInput(this, 0)
                    }
                    requestFocus()
                }
            },
            update = { view ->
                terminalSession.value?.let {
                    if (view.currentSession !== it) {
                        view.attachSession(it)
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        ExtraKeysBar(
            sessionHelper = sessionHelper,
            terminalViewRef = terminalViewRef,
            ctrlActive = ctrlActive,
            altActive = altActive,
            fnActive = fnActive
        )
    }
}

private fun parseColor(hex: String): Int {
    return android.graphics.Color.parseColor(hex)
}

@Composable
private fun ExtraKeysBar(
    sessionHelper: TerminalSessionHelper,
    terminalViewRef: androidx.compose.runtime.MutableState<TerminalView?>,
    ctrlActive: androidx.compose.runtime.MutableState<Boolean>,
    altActive: androidx.compose.runtime.MutableState<Boolean>,
    fnActive: androidx.compose.runtime.MutableState<Boolean>
) {
    val bgColor = Color(0xFF201B2D)
    val textColor = Color(0xFFE1E1E6)
    val activeBgColor = Color(0xFFE96379)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        StickyKey("ESC", "\\u001B", sessionHelper, bgColor, textColor)
        StickyKey("TAB", "\t", sessionHelper, bgColor, textColor)

        // Modifier keys (sticky toggle)
        ToggleKey("CTRL", ctrlActive, bgColor, textColor, activeBgColor)
        ToggleKey("ALT", altActive, bgColor, textColor, activeBgColor)
        ToggleKey("FN", fnActive, bgColor, textColor, activeBgColor)

        Spacer(modifier = Modifier.weight(1f))

        // Arrow keys
        ArrowKey("▲", KeyEvent.KEYCODE_DPAD_UP, sessionHelper, bgColor, textColor)
        ArrowKey("▼", KeyEvent.KEYCODE_DPAD_DOWN, sessionHelper, bgColor, textColor)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ArrowKey("◄", KeyEvent.KEYCODE_DPAD_LEFT, sessionHelper, bgColor, textColor)
        ArrowKey("►", KeyEvent.KEYCODE_DPAD_RIGHT, sessionHelper, bgColor, textColor)

        Spacer(modifier = Modifier.weight(1f))

        SpecialKey("~", "~", sessionHelper, bgColor, textColor)
        SpecialKey("/", "/", sessionHelper, bgColor, textColor)
        SpecialKey("-", "-", sessionHelper, bgColor, textColor)
        SpecialKey("HOME", "\\u001B[H", sessionHelper, bgColor, textColor)
        SpecialKey("END", "\\u001B[F", sessionHelper, bgColor, textColor)
    }
}

@Composable
private fun StickyKey(
    label: String,
    escapeSequence: String,
    sessionHelper: TerminalSessionHelper,
    bgColor: Color,
    textColor: Color
) {
    val resolved = escapeSequence
        .replace("\\u001B", "")
        .replace("\\t", "\t")
    Button(
        onClick = { sessionHelper.sendInput(resolved) },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun ToggleKey(
    label: String,
    active: androidx.compose.runtime.MutableState<Boolean>,
    bgColor: Color,
    textColor: Color,
    activeBgColor: Color
) {
    Button(
        onClick = { active.value = !active.value },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active.value) activeBgColor else bgColor,
            contentColor = textColor
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun ArrowKey(
    label: String,
    keyCode: Int,
    sessionHelper: TerminalSessionHelper,
    bgColor: Color,
    textColor: Color
) {
    Button(
        onClick = { sessionHelper.sendKeyDownUp(keyCode) },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun SpecialKey(
    label: String,
    input: String,
    sessionHelper: TerminalSessionHelper,
    bgColor: Color,
    textColor: Color
) {
    val resolved = input.replace("\\u001B", "")
    Button(
        onClick = { sessionHelper.sendInput(resolved) },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

private class TerminalClient(
    private val ctrlActive: androidx.compose.runtime.MutableState<Boolean>,
    private val altActive: androidx.compose.runtime.MutableState<Boolean>,
    private val fnActive: androidx.compose.runtime.MutableState<Boolean>,
    private val sessionHelper: TerminalSessionHelper
) : TerminalViewClient {

    override fun onScale(scale: Float): Float = scale.coerceIn(0.5f, 3.0f)
    override fun onSingleTapUp(e: MotionEvent?) {}
    override fun shouldBackButtonBeMappedToEscape() = false
    override fun shouldEnforceCharBasedInput() = true
    override fun shouldUseCtrlSpaceWorkaround() = false
    override fun isTerminalViewSelected() = true
    override fun copyModeChanged(copyMode: Boolean) {}

    override fun onEmulatorSet() {
        sessionHelper.applyColorsToView()
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?) = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent?) = false
    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?) = false
    override fun onLongPress(e: MotionEvent?) = false

    override fun readControlKey() = ctrlActive.value
    override fun readAltKey() = altActive.value
    override fun readShiftKey() = false
    override fun readFnKey() = fnActive.value

    override fun logError(tag: String?, msg: String?) {}
    override fun logWarn(tag: String?, msg: String?) {}
    override fun logInfo(tag: String?, msg: String?) {}
    override fun logDebug(tag: String?, msg: String?) {}
    override fun logVerbose(tag: String?, msg: String?) {}
    override fun logStackTraceWithMessage(tag: String?, msg: String?, e: Exception?) {}
    override fun logStackTrace(tag: String?, e: Exception?) {}
}