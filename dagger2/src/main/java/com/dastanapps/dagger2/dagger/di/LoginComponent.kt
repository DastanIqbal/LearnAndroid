package com.dastanapps.dagger2.dagger.di

import com.dastanapps.dagger2.MainActivity
import com.dastanapps.dagger2.dagger.LoginFragment
import dagger.Subcomponent

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
@ActivityScope
@Subcomponent
interface LoginComponent {

    // Factory that is used to create instances of this subcomponent
    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginComponent
    }

    fun inject(mainActivity: MainActivity)
    fun inject(loginFragment: LoginFragment)
}