package com.dastanapps.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dastanapps.hilt.MainViewModel
import com.dastanapps.hilt.databinding.FragmentMainBinding
import javax.inject.Inject

/**
 * Activity that displays SDK content.
 * Injection is handled by SdkComponent.
 */
class SdkActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inject dependencies
        SdkMiddleware.getComponent().inject(this)

        super.onCreate(savedInstanceState)
        val binding = FragmentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.greetingText.text = viewModel.greeting
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, SdkActivity::class.java)
            context.startActivity(intent)
        }
    }
}
