package com.armmali.graphicssetup

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * Created by dastaniqbal on 04/01/2018.
 * dastanIqbal@marvelmedia.com
 * 04/01/2018 11:36
 */
class GraphicsSetup : Activity() {

    val TAG = GraphicsSetup::class.simpleName
    protected lateinit var tutorialView: TutorialView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialView = TutorialView(this)
        setContentView(tutorialView)
        Log.d(TAG, "On Create Method Called Native Library")
    }

    override fun onPause() {
        super.onPause()
        tutorialView.onPause()
    }

    override fun onResume() {
        super.onResume()
        tutorialView.onResume()
    }
}
