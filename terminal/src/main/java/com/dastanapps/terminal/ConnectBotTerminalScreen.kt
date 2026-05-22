package com.dastanapps.terminal

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.connectbot.terminal.DelKeyMode
import org.connectbot.terminal.RightAltMode
import org.connectbot.terminal.Terminal

@Composable
fun ConnectBotTerminalContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessionHelper = remember { ConnectBotSessionHelper(context) }
    val emulator = remember { sessionHelper.createEmulator() }
    val focusRequester = remember { FocusRequester() }
    var selectedScheme by remember { mutableStateOf(TerminalColorScheme.Dracula) }
    var showThemeMenu by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        sessionHelper.startShell()
        onDispose {
            sessionHelper.destroy()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(selectedScheme) {
        // Re-request focus after theme change since Terminal may have been recreated
        focusRequester.requestFocus()
    }

    val fgColor = remember(selectedScheme) { Color(AndroidColor.parseColor(selectedScheme.foreground)) }
    val bgColor = remember(selectedScheme) { Color(AndroidColor.parseColor(selectedScheme.background)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
    ) {
        // Theme picker bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Button(
                    onClick = { showThemeMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2540),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(selectedScheme.name, fontSize = 10.sp)
                }
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    TerminalColorScheme.all.forEach { scheme ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (scheme.name == selectedScheme.name) Text("") else Text("  ")
                                    Text(scheme.name)
                                }
                            },
                            onClick = {
                                selectedScheme = scheme
                                sessionHelper.applyColorScheme(scheme)
                                sessionHelper.updatePrompt()
                                showThemeMenu = false
                            }
                        )
                    }
                }
            }
        }

        key(selectedScheme.name) {
            Terminal(
                terminalEmulator = emulator,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable {
                        focusRequester.requestFocus()
                        val imm = context.getSystemService(InputMethodManager::class.java)
                        imm?.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT)
                    },
                typeface = Typeface.MONOSPACE,
                initialFontSize = 16.sp,
                backgroundColor = bgColor,
                foregroundColor = fgColor,
                keyboardEnabled = true,
                showSoftKeyboard = true,
                modifierManager = sessionHelper.modifierManager,
                rightAltMode = RightAltMode.CharacterModifier,
                delKeyMode = DelKeyMode.Delete,
                focusRequester = focusRequester,
                onTerminalTap = {
                    focusRequester.requestFocus()
                    val imm = context.getSystemService(InputMethodManager::class.java)
                    imm?.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT)
                }
            )
        }

        ConnectBotExtraKeysBar(sessionHelper = sessionHelper)
    }
}

@Composable
private fun ConnectBotExtraKeysBar(
    sessionHelper: ConnectBotSessionHelper
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
        ConnectBotStickyKey("ESC", "", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("TAB", "\t", sessionHelper, bgColor, textColor)

        ConnectBotToggleKey("CTRL", sessionHelper.ctrlState.value, sessionHelper::toggleCtrl, bgColor, textColor, activeBgColor)
        ConnectBotToggleKey("ALT", sessionHelper.altState.value, sessionHelper::toggleAlt, bgColor, textColor, activeBgColor)

        Spacer(modifier = Modifier.weight(1f))

        ConnectBotStickyKey("▲", "[A", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("▼", "[B", sessionHelper, bgColor, textColor)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ConnectBotStickyKey("◀", "[D", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("▶", "[C", sessionHelper, bgColor, textColor)

        Spacer(modifier = Modifier.weight(1f))

        ConnectBotStickyKey("~", "~", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("/", "/", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("-", "-", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("HOME", "[H", sessionHelper, bgColor, textColor)
        ConnectBotStickyKey("END", "[F", sessionHelper, bgColor, textColor)
    }
}

@Composable
private fun ConnectBotStickyKey(
    label: String,
    input: String,
    sessionHelper: ConnectBotSessionHelper,
    bgColor: Color,
    textColor: Color
) {
    Button(
        onClick = { sessionHelper.sendInput(input) },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun ConnectBotToggleKey(
    label: String,
    isActive: Boolean,
    onToggle: () -> Boolean,
    bgColor: Color,
    textColor: Color,
    activeBgColor: Color
) {
    Button(
        onClick = { onToggle() },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeBgColor else bgColor,
            contentColor = textColor
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}