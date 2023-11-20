package com.dastanapps.oldnavigation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.oldnavigation.ui.main.MainFragment
import com.dastanapps.oldnavigation.ui.main.toast
import com.payfort.fortpaymentsdk.callbacks.FortCallBackManager
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val fortCallback: FortCallBackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .addToBackStack("dd")
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("OldNavigation", "Activity BackPressed")
        toast("Activity BackPressed")
    }
}