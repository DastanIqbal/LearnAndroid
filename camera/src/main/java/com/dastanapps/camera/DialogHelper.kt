package com.dastanapps.camera

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.widget.ArrayAdapter

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 10:16
 */

class DialogHelper {
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            val activity = activity
            return AlertDialog.Builder(activity)
                    .setMessage(arguments.getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i -> activity.finish() }
                    .create()
        }

        companion object {

            private val ARG_MESSAGE = "message"

            fun newInstance(message: String): ErrorDialog {
                val dialog = ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }

    }

    class FeatureListDialog : DialogFragment() {

        private var arrayAdapter: ArrayAdapter<String>? = null
        private var features: List<String>? = null
        private var listener: ISelectItem? = null

        interface ISelectItem {
            fun onSelectItem(which: Int)
        }

        fun setFeatures(features: List<String>, iSelectItem: ISelectItem): FeatureListDialog {
            this.features = features
            this.listener = iSelectItem
            return this
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            arrayAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, features!!)
            return AlertDialog.Builder(activity)
                    .setAdapter(arrayAdapter) { dialog, which ->
                        dialog.dismiss()
                        listener!!.onSelectItem(which)
                    }
                    .create()
        }

        companion object {

            fun newInstance(): FeatureListDialog {
                val dialog = FeatureListDialog()
                val args = Bundle()
                return dialog
            }
        }

    }
}
