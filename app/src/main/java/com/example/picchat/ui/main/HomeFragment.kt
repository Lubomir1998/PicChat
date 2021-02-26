package com.example.picchat.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.picchat.R
import com.example.picchat.databinding.HomeFragmentBinding
import com.example.picchat.other.Constants.KEY_EMAIL
import com.example.picchat.other.Constants.KEY_PASSWORD
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_EMAIL
import com.example.picchat.other.Constants.NO_PASSWORD
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.ui.auth.AuthActivity
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            logout()
        }


    }

    private fun logout() {
        sharedPrefs.edit()
            .putString(KEY_UID, NO_UID)
            .putString(KEY_EMAIL, NO_EMAIL)
            .putString(KEY_PASSWORD, NO_PASSWORD)
            .apply()

        startActivity(Intent(requireContext(), AuthActivity::class.java))
    }

}