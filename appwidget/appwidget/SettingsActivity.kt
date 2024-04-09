package com.dastanapps.appwidget

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import android.view.View
import android.widget.ListView

/* loaded from: classes.dex */
class SettingsActivity : PreferenceActivity(), OnPreferenceClickListener {
    // android.preference.PreferenceActivity, android.app.Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        val volume = findPreference(VOLUME_SOURCE_KEY) as ListPreference
        volume.entryValues =
            arrayOf(2.toString(), 3.toString(), 4.toString(), 5.toString())
        val batteryNotice = findPreference(REFRESH_NOTICE_KEY)
        batteryNotice.onPreferenceClickListener = this
        val aboutNotice = findPreference(ABOUT_NOTICE_KEY)
        aboutNotice.onPreferenceClickListener = this
        val helpNotice = findPreference(HELP_NOTICE_KEY)
        helpNotice.onPreferenceClickListener = this
    }

    // android.preference.PreferenceActivity, android.app.ListActivity
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
    }

    // android.preference.Preference.OnPreferenceClickListener
    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference.key == REFRESH_NOTICE_KEY) {
            showMessageBox(R.string.refresh_notice_title, R.string.refresh_notice_text)
            return true
        } else if (preference.key == ABOUT_NOTICE_KEY) {
            showMessageBox(R.string.about_title, R.string.about_text)
            return true
        } else if (preference.key == HELP_NOTICE_KEY) {
            showMessageBox(R.string.help_title, R.string.help_text)
            return true
        } else {
            return true
        }
    }

    private fun showMessageBox(title: Int, message: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setPositiveButton(17039370, null as DialogInterface.OnClickListener?)
        builder.setMessage(message)
        builder.create().show()
    }

    companion object {
        private const val ABOUT_NOTICE_KEY = "ABOUT_NOTICE"
        private const val HELP_NOTICE_KEY = "HELP_NOTICE"
        private const val REFRESH_NOTICE_KEY = "REFRESH_NOTICE"
        private const val VOLUME_SOURCE_KEY = "CTW_VOLUME_SOURCE"
    }
}