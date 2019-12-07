package com.dastanapps.dagger2.dagger

import com.dastanapps.dagger2.dagger.di.ActivityScope
import com.dastanapps.dagger2.dagger.repo.UserRepo
import javax.inject.Inject

@ActivityScope
class LoginViewModel @Inject constructor(private val userRepo: UserRepo) {

    fun login() {
        userRepo.login()
    }
}
