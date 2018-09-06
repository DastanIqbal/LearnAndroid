package com.iaandroid.tutsopengles.frames

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.dastanapps.mediasdk.opengles.gpu.ImageRenderer
import com.dastanapps.mediasdk.opengles.gpu.filter.InvertColorFilter
import com.dastanapps.mediasdk.opengles.gpu.filter.NoneFilter
import com.dastanapps.mediasdk.opengles.gpu.filter.SharpenFilter
import com.iaandroid.tutsopengles.R
import kotlinx.android.synthetic.main.activity_frames.*

/**
 * Created by dastan on 05/09/2018.
 * ask2iqbal@gmail.com
 * 05/09/2018 11:23
 */
class FramesActivity : Activity() {
    private val glSurfaceView: GLSurfaceView by lazy {
        findViewById<GLSurfaceView>(R.id.glsurfaceview)
    }

    private val renderer:ImageRenderer by lazy{
        ImageRenderer(NoneFilter(NoneFilter.defalutVS,NoneFilter.defalutFS))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_frames)
        glSurfaceView.setEGLContextClientVersion(2)
        renderer.setGLSurfaceView(glSurfaceView)
        glSurfaceView.setRenderer(renderer)
        renderer.setTexture(
                BitmapFactory.decodeResource(resources, R.drawable.image),
                false
        )

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = MyFiltersAdapter(this, populateFitlers(),renderer)
    }

//    override fun onPause() {
//        super.onPause()
//        glSurfaceView.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        glSurfaceView.onResume()
//    }

    fun populateFitlers(): ArrayList<FiltersB> {
        val filtersList = ArrayList<FiltersB>()
        filtersList.add(FiltersB("None", NoneFilter(NoneFilter.defalutVS, NoneFilter.defalutFS)))
        filtersList.add(FiltersB("Invert", InvertColorFilter()))
        filtersList.add(FiltersB("Sharpen", SharpenFilter()))
        return filtersList
    }

    data class FiltersB(var name: String, var filter: NoneFilter)

    class MyFiltersAdapter(
            private val context: Context,
            private val filtersList: ArrayList<FiltersB>,
            val renderer: ImageRenderer
    ) : RecyclerView.Adapter<MyFiltersAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false))
        }

        override fun getItemCount(): Int = filtersList.size

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.bind(filtersList[p1])
        }

        inner class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            fun bind(filtersB: FiltersB) {
                (view as TextView).text = filtersB.name
                view.setOnClickListener {
                    renderer.setFilter(filtersB.filter)
                }
            }
        }
    }
}