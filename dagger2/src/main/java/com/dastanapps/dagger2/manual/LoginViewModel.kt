package com.dastanapps.dagger2.manual

import com.dastanapps.dagger2.manual.repo.UserRepo

class LoginViewModel(private val userRepo: UserRepo) {

    fun login() {
        userRepo.login()
    }
}
