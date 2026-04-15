package com.dastanapps.hilt_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.sdk.SdkActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SdkActivity.launch(this)
        finish()
    }
}
