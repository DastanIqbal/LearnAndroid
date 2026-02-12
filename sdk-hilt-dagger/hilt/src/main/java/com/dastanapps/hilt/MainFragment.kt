package com.dastanapps.hilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dastanapps.hilt.databinding.FragmentMainBinding
import javax.inject.Inject

/**
 * Fragment that uses field injection via Dagger.
 * Injection is handled by SdkFragmentFactory after instantiation.
 * Hilt annotations (@HiltViewModel, @InstallIn) remain for use in actual Hilt apps.
 */
class MainFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.greetingText.text = viewModel.greeting
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
