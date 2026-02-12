package com.dastanapps.sdk

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.hilt.MainFragment

/**
 * Intermediate Activity that hosts fragments for the SDK.
 * Uses FragmentFactory to manually instantiate fragments with Dagger dependencies.
 * Works in both Hilt and non-Hilt applications.
 */
class SdkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Init SDK
        SdkMiddleware.init()
        
        // Install Manual Fragment Registry
        supportFragmentManager.fragmentFactory = SdkMiddleware.getComponent().getFragmentFactory()

        super.onCreate(savedInstanceState)
        
        val container = FrameLayout(this).apply {
            id = R.id.content
        }
        setContentView(container)

        if (savedInstanceState == null) {
            // Instantiate via factory
            val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                MainFragment::class.java.name
            )
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commitNow()
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, SdkActivity::class.java)
            context.startActivity(intent)
        }
    }
}
