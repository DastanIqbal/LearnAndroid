package com.dastanapps.integrated_app

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.sdk.SdkMiddleware

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Simple button to launch SDK's activity
        val button = Button(this).apply {
            text = "Open SDK Activity"
            setOnClickListener {
                SdkMiddleware.launchMainFragment(this@MainActivity)
            }
        }
        setContentView(button)
    }
}
