package com.dastanapps.tencent_miniapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.dastanapps.tencent_miniapp.ui.theme.LearnAndroidTheme
import com.tencent.tmf.mini.api.TmfMiniSDK
import com.tencent.tmf.mini.api.bean.MiniApp
import com.tencent.tmf.mini.api.bean.MiniScene
import com.tencent.tmf.mini.api.bean.MiniStartOptions


val appID = "mpolm7nemgza6mwr"
val appParams = """
    
""".trimIndent()

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<ComposeView>(R.id.compose_view).apply {
            setContent {
                LearnAndroidTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier
                                .padding(innerPadding)
                                .clickable {
                                    openMiniApp()
                                }
                        )
                    }
                }
            }
        }
    }

    fun openMiniApp(){
        val startOption = MiniStartOptions()
        startOption.params = appParams

        TmfMiniSDK.startMiniApp(
            this,
            appID,
            MiniScene.LAUNCH_SCENE_MAIN_ENTRY,
            MiniApp.TYPE_ONLINE,
            startOption
        )
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