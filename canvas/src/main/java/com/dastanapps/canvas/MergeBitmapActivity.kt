package com.dastanapps.canvas

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.dastanapps.dastanlib.utils.ViewUtils
import com.dastanapps.mediasdk.opengles.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_merge_bitmap.*
import java.io.File

class MergeBitmapActivity : AppCompatActivity() {
    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    }

    var bitmap1: Bitmap? = null
    var bitmap2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_bitmap)

        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            pickImage(this)
        }

        button.setOnClickListener {
            val bmp = BitmapUtils.overlay(bitmap1, bitmap2)
            bmp?.run {
                imageView3.setImageBitmap(this)
            }
        }
        button2.setOnClickListener {
            pickImage(this)
        }
        button3.setOnClickListener {
            bitmap1 = null
            bitmap2 = null
        }
    }


    private fun pickImage(ctxt: Context) {
        val packageManager = ctxt.packageManager
        val intent = Intent(Intent.ACTION_PICK, null)//MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            try {
                (ctxt as Activity).startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }

        } else {
            ViewUtils.showToast(ctxt, "No Application Found to Open Video")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            if (uri != null) {
                val imageFile = uriToImageFile(uri)
                val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
                setImageBitmap(bitmap)
            }
            if (uri != null) {
                val imageBitmap = uriToBitmap(uri)
                setImageBitmap(imageBitmap)
            }
        }
    }

    private fun setImageBitmap(bitmap: Bitmap?) {
        if (bitmap1 == null) {
            imageView.setImageBitmap(bitmap)
            bitmap1 = bitmap
        } else if (bitmap2 == null) {
            imageView2.setImageBitmap(bitmap)
            bitmap2 = bitmap
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // pick image after request permission success
                    pickImage(this)
                }
            }
        }
    }

    private fun uriToImageFile(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val filePath = cursor.getString(columnIndex)
                cursor.close()
                return File(filePath)
            }
            cursor.close()
        }
        return null
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }
}
