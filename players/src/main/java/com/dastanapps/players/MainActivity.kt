package com.dastanapps.players

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import com.flow.framework.PspManager
import com.function.product.cm.util.MemoryUtil
import kr.co.namee.permissiongen.PermissionFail
import kr.co.namee.permissiongen.PermissionGen
import kr.co.namee.permissiongen.PermissionSuccess



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.listview)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (position == 0) {
                startActivity(Intent(this, ExoPlayer::class.java))
            } else if (position == 1) {
                startActivity(Intent(this, MediaPlayerActivity::class.java))
            }
        }

        //requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            PspManager.getInstance().initSDK()
        },3000)
    }
    private fun requestPermissions() {
        PermissionGen.with(this as Activity).addRequestCode(100).permissions("android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.SEND_SMS", "android.permission.READ_SMS").request()
    }

    @PermissionSuccess(requestCode = 100)
    public fun test() {
        Log.e("TAG", "PermissionSuccess");
        PspManager.getInstance().initSDK();
    }

    @PermissionFail(requestCode = 100)
    private fun test2() {
        Log.e("TAG", "PermissionFail")
        requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionGen.onRequestPermissionsResult(this as Activity, requestCode, permissions, grantResults)
    }

    private fun initData() {
        val storageMemory = MemoryUtil.getUsedAndTotalStorage(this)
        val storagePercent = MemoryUtil.getStoragePercent()
        val ramMemory = MemoryUtil.getUsedAndTotalRAM(this)
        val ramPercent = MemoryUtil.getRAMPercent(this)
        val availableSpace = MemoryUtil.getAvailableSpace()
        Log.d("debug","Storage Memory: $storageMemory $storagePercent% RamMemory: $ramMemory $ramPercent% AvailSpaces: $availableSpace")
    }
}
