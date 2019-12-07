package com.dastanapps.dagger2.manual

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dastanapps.dagger2.App
import com.dastanapps.dagger2.R

class Login2Fragment : Fragment() {

    companion object {
        fun newInstance() = Login2Fragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("Login2Fragment", "AppContainer")
        //AppContainer to maintain dependencies across app

        //val appContainer = (requireActivity().application as App).appContainer
        //viewModel = LoginViewModel(appContainer.userRepo)

        val loginViewModel = (requireActivity().application as App).appContainer.loginViewModel
        viewModel = loginViewModel
        viewModel.login()
    }

}
