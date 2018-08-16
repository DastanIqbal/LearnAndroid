package com.dastanapps.androidservices

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dastanapps.services.MyBoundService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mBoundService: MyBoundService? = null
    private var mShouldBind = false
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            mBoundService = null
            showMsg("Service Disconnected")
        }

        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            mBoundService = (service as MyBoundService.LocalBinder).getService()
            mBoundService?.showNotification()
            showMsg("Service Connected")
        }
    }

    private fun doBindService() {
        if (bindService(Intent(this, MyBoundService::class.java),
                        mServiceConnection,
                        Context.BIND_AUTO_CREATE)) {
            mShouldBind = true

        } else {
            showMsg("Service not bind")
        }
    }

    private fun doUnbindService() {
        if (mShouldBind) {
            unbindService(mServiceConnection)
            mShouldBind = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = MyAdapter()
    }

    override fun onStop() {
        super.onStop()
        doUnbindService()
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
        private val serviceList = ArrayList<String>()
        private val ctxt = this@MainActivity

        init {
            serviceList.add("Service")
            serviceList.add("BoundService")
            serviceList.add("IntentService")
            serviceList.add("JobIntentService")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(ctxt).inflate(android.R.layout.simple_list_item_1, parent, false))
        }

        override fun getItemCount() = serviceList.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(serviceList[position])
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView = (itemView as AppCompatTextView)
            fun bind(name: String) {
                textView.text = name
                textView.setOnClickListener {
                    when (adapterPosition) {
                        0 -> {
                            showMsg("Service")
                        }
                        1 -> {
                            showMsg("Bound Service")
                            doBindService()
                        }
                        2 -> showMsg("IntentService")
                        3 -> showMsg("JobIntentService")
                    }
                }

            }
        }
    }

    fun showMsg(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
    }
}
