package com.dastanapps.verticaledittext

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var userString: StringBuilder = StringBuilder();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("DEBUG:afterTextChanged", s.toString());
                Log.d("DEBUG:finalString", userString.toString());
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("DEBUG:beforeTextChanged", s.toString() + " $start Start, $count Count ,$after After");
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("DEBUG:onTextChanged", s.toString() + " $start Start, $count Count ,$before Before");
                userString.append(s!![before]).append("\n")
            }
        })
    }
}
