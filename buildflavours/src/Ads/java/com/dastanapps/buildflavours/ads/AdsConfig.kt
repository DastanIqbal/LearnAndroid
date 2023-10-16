package com.dastanapps.buildflavours.ads

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dastanapps.buildflavours.BuildConfig

class AdsConfig {

    fun callMe(context: Context): String {
        val message = "Current Flavour ${BuildConfig.FLAVOR}"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.d("DEBUG", message)
        return message
    }
}