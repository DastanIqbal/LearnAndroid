package com.dastanapps.socketio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dastanapps.socketio.ui.theme.LearnAndroidTheme
import io.socket.client.Manager
import io.socket.engineio.client.Socket
import java.util.logging.Level
import java.util.logging.Logger


class MainActivity : ComponentActivity() {
    private val socketManager by lazy {
        SocketIoManager(BuildConfig.SOCKET_URL)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        logcat()
        socketManager
        setContent {
            LearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun logcat() {
        AndroidLoggingHandler.reset(AndroidLoggingHandler())
        Logger.getLogger(Socket::class.java.getName()).level = Level.FINEST
        Logger.getLogger(Socket::class.java.name).level = Level.FINEST
        Logger.getLogger(Manager::class.java.name).level = Level.FINEST

    }

    override fun onStop() {
        super.onStop()
        socketManager.disconnect()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LearnAndroidTheme {
        Greeting("Android")
    }
}