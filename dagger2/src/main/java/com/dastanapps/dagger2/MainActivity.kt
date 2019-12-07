package com.dastanapps.dagger2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.dagger2.dagger.di.LoginComponent

class MainActivity : AppCompatActivity() {

    lateinit var loginComponent: LoginComponent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginComponent = (application as App).appGraph.loginComponent().create()
        loginComponent.inject(this)
    }
}
