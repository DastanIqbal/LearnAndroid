package com.dastanapps.dagger2.dagger

import com.dastanapps.dagger2.dagger.repo.UserRepo

class LoginViewModel(private val userRepo: UserRepo) {

    fun login() {
        userRepo.login()
    }
}
