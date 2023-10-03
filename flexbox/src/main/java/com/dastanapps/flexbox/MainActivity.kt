package com.dastanapps.flexbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dastanapps.flexbox.R.*
import com.dastanapps.flexbox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layout.activity_main)
        ui()
    }

    private fun ui() {
        binding.rv.withModels {

        }
    }
}