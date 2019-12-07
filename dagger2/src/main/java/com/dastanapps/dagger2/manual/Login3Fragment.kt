package com.dastanapps.dagger2.manual

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dastanapps.dagger2.App
import com.dastanapps.dagger2.R

class Login3Fragment : Fragment() {

    companion object {
        fun newInstance() = Login3Fragment()
    }

    private lateinit var appContainer: AppContainer
    private lateinit var loginContainer: LoginContainer
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("Login3Fragment", "AppContainer")

        appContainer = (requireActivity().application as App).appContainer
        appContainer.loginContainer = LoginContainer(appContainer.userRepo)
        viewModel = appContainer.loginContainer?.loginViewModelFactory?.create()!!
        viewModel.login()
    }

    override fun onDestroy() {
        super.onDestroy()
        appContainer.loginContainer = null
    }

}
