package com.dastanapps.dagger2.dagger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dastanapps.dagger2.R
import com.dastanapps.dagger2.dagger.di.DaggerAppGraph

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("LoginFragment", "Dagger Injection")

        val appGraph = DaggerAppGraph.create()
        val userRepo = appGraph.userRepo()

        viewModel = LoginViewModel(userRepo)
        viewModel.login()
    }

}
