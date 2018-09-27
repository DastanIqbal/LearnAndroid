package com.armmali.firstnative

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.iaandroid.tutsopengles.R
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by dastaniqbal on 04/01/2018.

 * 04/01/2018 11:36
 */
class FirstNative : Activity() {

    val TAG = FirstNative::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_hello.text = NativeLibrary.init()
        Log.d(TAG, "On Create Method Called Native Library")
    }
}
