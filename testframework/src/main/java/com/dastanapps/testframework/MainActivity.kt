package com.dastanapps.testframework

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : ListActivity() {

    var testsActivity: Array<String> = arrayOf("LifeCycleTest", "SingleTouchTest", "MultiTouchTest",
            "KeyTest", "AccelerometerTest", "AssetsTest", "ExternalStorageTest", "SoundPoolTest",
            "MediaPlayerTest", "FullScreenTest", "RenderViewTest", "ShapeTest", "BitmapTest",
            "FontTest", "SurfaceViewTest")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testsActivity);
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        startActivity(Intent(this, Class.forName("com.dastanapps.testframework.test." + testsActivity[position])))
    }
}
