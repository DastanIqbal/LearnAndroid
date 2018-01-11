package com.armmali.graphicssetup

import android.app.Activity
import android.os.Bundle
import android.util.Log
import java.io.File
import java.io.RandomAccessFile

/**
 * Created by dastaniqbal on 04/01/2018.
 * dastanIqbal@marvelmedia.com
 * 04/01/2018 11:36
 */
class GraphicsSetup : Activity() {

    val TAG = GraphicsSetup::class.simpleName
    protected lateinit var tutorialView: TutorialView
    private var assetDirectory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialView = TutorialView(this)
        /* [onCreateNew] */
        assetDirectory = applicationContext.filesDir.path + "/"


        extractAsset("normalMap256.raw")

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

    /* [extractAssetBeginning] */
    private fun extractAsset(assetName: String) {
        val fileTest = File(assetDirectory + assetName)

        if (fileTest.exists()) {
            Log.d(TAG, assetName + " already exists no extraction needed\n")
        } else {
            Log.d(TAG, assetName + " doesn't exist extraction needed \n")
            /* [extractAssetBeginning] */
            /* [tryCatchExtractAsset] */
            try {
                val out = RandomAccessFile(assetDirectory + assetName, "rw")
                val am = assets
                /* [tryCatchExtractAsset] */
                /* [readWriteFile] */
                val inputStream = am.open(assetName)
                val buffer = ByteArray(1024)
                var count = inputStream.read(buffer, 0, 1024)

                while (count > 0) {
                    out.write(buffer, 0, count)
                    count = inputStream.read(buffer, 0, 1024)
                }
                out.close()
                inputStream.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failure in extractAssets(): " + e.toString() + " " + assetDirectory + assetName)
            }

            if (fileTest.exists()) {
                Log.d(TAG, "File Extracted successfully")
                /* [readWriteFile] */
            }
        }
    }
}
