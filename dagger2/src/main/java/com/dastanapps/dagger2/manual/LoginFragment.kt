package com.dastanapps.dagger2.manual

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dastanapps.dagger2.R
import com.dastanapps.dagger2.manual.repo.LocalDataSource
import com.dastanapps.dagger2.manual.repo.RemoteDataSource
import com.dastanapps.dagger2.manual.repo.Retrofit
import com.dastanapps.dagger2.manual.repo.UserRepo

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
        Log.d("LoginFragment", "AppContainer")

        val retrofit = Retrofit()
        val localDataSource = LocalDataSource()
        val remoteDataSource = RemoteDataSource(retrofit)
        val userRepo = UserRepo(localDataSource, remoteDataSource)

        viewModel = LoginViewModel(userRepo)
        viewModel.login()
    }

}
