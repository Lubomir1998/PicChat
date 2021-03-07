package com.example.picchat.ui.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.picchat.R
import com.example.picchat.databinding.RegisterFragmentBinding
import com.example.picchat.other.Constants.KEY_USERNAME
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.register_fragment) {

    private lateinit var binding: RegisterFragmentBinding
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLogin.setOnClickListener {
            if(findNavController().previousBackStackEntry != null) {
                findNavController().popBackStack()
            }
            else {
                findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                )
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etRepeatPassword.text.toString().trim()

            sharedPrefs.edit().putString(KEY_USERNAME, username).apply()

            viewModel.register(username, email, password, confirmPassword)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.registerState.collect {

                when(val result = it.peekContent()) {

                    is Resource.Success -> {
                        binding.registerProgressBar.isVisible = false
                        snackbar(result.message ?: "Successfully created account")
                    }

                    is Resource.Error -> {
                        binding.registerProgressBar.isVisible = false

                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.registerProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }

            }
        }


    }


}