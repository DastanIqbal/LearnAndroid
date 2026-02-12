package com.dastanapps.sdk

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.dastanapps.hilt.MainFragment
import javax.inject.Inject

/**
 * Manual Fragment Registry for the SDK.
 * Instantiates fragments, then injects fields using Dagger component.
 */
class SdkFragmentFactory @Inject constructor(
    private val component: SdkComponent
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            MainFragment::class.java.name -> {
                val fragment = MainFragment()
                component.inject(fragment)
                fragment
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}
