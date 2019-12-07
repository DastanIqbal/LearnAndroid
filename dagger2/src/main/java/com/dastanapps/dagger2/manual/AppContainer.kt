package com.dastanapps.dagger2.manual

import com.dastanapps.dagger2.manual.repo.LocalDataSource
import com.dastanapps.dagger2.manual.repo.RemoteDataSource
import com.dastanapps.dagger2.manual.repo.Retrofit
import com.dastanapps.dagger2.manual.repo.UserRepo

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class AppContainer {
    private val TAG = this::class.java.simpleName
    private val retrofit = Retrofit()
    private val localDataSource = LocalDataSource()
    private val remoteDataSource = RemoteDataSource(retrofit)
    val userRepo = UserRepo(localDataSource, remoteDataSource)

    var loginContainer: LoginContainer? = null
}

class LoginContainer(private val userRepo: UserRepo) {
    val loginUserData = LoginUserData()
    val loginViewModelFactory = LoginViewModelFactory(userRepo)
}

class LoginUserData {

}