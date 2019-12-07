package com.dastanapps.dagger2.manual

import com.dastanapps.dagger2.manual.repo.UserRepo

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class LoginViewModelFactory(private val userRepo: UserRepo) : Factory<LoginViewModel> {
    override fun create(): LoginViewModel {
        return LoginViewModel(userRepo)
    }

    private val TAG = this::class.java.simpleName
}