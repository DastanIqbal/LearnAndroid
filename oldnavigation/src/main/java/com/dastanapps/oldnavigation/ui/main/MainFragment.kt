package com.dastanapps.oldnavigation.ui.main

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.dastanapps.oldnavigation.R
import com.dastanapps.oldnavigation.databinding.FragmentMainBinding
import kotlin.random.Random

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentMainBinding?=null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().toast("Main Fragment")
        binding.message.setOnClickListener {
//            requireActivity().supportFragmentManager.beginTransaction()
//                .add(R.id.container, MainFragment.newInstance(), "Main $")
//                .addToBackStack("stuck")
//                .commit()

            childFragmentManager.beginTransaction()
                .add(R.id.containerChild, MainFragment.newInstance(), "Main $")
                .addToBackStack("stuck")
                .commit()
        }
    }
}

fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}