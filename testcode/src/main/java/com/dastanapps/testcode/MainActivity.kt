package com.dastanapps.testcode

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.dastanapps.dastanlib.utils.ViewUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv).setOnClickListener {
            ViewUtils.showToast(this, "Hellya You touched")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            startActivityForResult(intent, 1001)
        } else {
            startService(Intent(this, DrawOverlayService::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && !Settings.canDrawOverlays(this)) {
            checkPermission()
        } else if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, DrawOverlayService::class.java))
        }
    }
}
