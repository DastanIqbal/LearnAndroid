package com.dastanapps.androidp.subscription

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import com.dastanapps.androidp.R
import com.dastanapps.androidp.databinding.DialogTryfreeBinding

/**
 * Created by dastaniqbal on 07/05/2019.
 * 07/05/2019 10:54
 */
class TryFreeDialog(private val context: Context) {
    private val TAG = this::class.java.simpleName

    private val tryfreeDialog = getMDialog(context, R.layout.dialog_tryfree, Gravity.CENTER)
    private val binding = DialogTryfreeBinding.bind(tryfreeDialog.findViewById(R.id.fl))
    var isdismissed = false

    init {
        tryfreeDialog.setCancelable(false)
        tryfreeDialog.setCanceledOnTouchOutside(false)

        binding.imvCancel.setOnClickListener {
            tryfreeDialog.dismiss()
            isdismissed = true
        }
    }

    fun getMDialog(ctxt: Context, reslayout: Int, gravity: Int): Dialog {
        val d = Dialog(ctxt, R.style.Theme_Dialog)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = d.window
        d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val wlp = WindowManager.LayoutParams()
        wlp.copyFrom(window!!.attributes)
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT
        wlp.gravity = gravity//Gravity.BOTTOM;
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        d.setContentView(reslayout)
        return d
    }

    fun show() {
        if (!tryfreeDialog.isShowing)
            tryfreeDialog.show()
    }

    fun dismiss() {
        tryfreeDialog.dismiss()
    }

    fun handleBack(invoke: () -> Unit) {
        tryfreeDialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                invoke.invoke()
                dialog.dismiss()
            }
            true
        }
    }
}