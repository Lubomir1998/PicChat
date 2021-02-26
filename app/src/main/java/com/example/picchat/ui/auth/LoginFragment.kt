package com.example.picchat.ui.auth

import android.content.Intent
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
import com.example.picchat.databinding.LoginFragmentBinding
import com.example.picchat.other.BasicAuthInterceptor
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.KEY_EMAIL
import com.example.picchat.other.Constants.KEY_PASSWORD
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.ui.main.MainActivity
import com.example.picchat.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.login_fragment) {

    private lateinit var binding: LoginFragmentBinding
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var currentEmail: String? = null
    private var currentPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvRegisterNewAccount.setOnClickListener {
            if(findNavController().previousBackStackEntry != null) {
                findNavController().popBackStack()
            }
            else {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                )
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etPassword.text.toString()

            currentEmail = email
            currentPassword = password

            viewModel.login(email, password)
        }


        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.loginProgressBar.isVisible = false
                        snackbar(result.message ?: "Successfully logged in")

                        sharedPrefs.edit()
                            .putString(KEY_EMAIL, currentEmail)
                            .putString(KEY_PASSWORD, currentPassword)
                            .apply()

                        authenticateApi(currentEmail ?: "", currentPassword ?: "")

                        viewModel.getUid()

                        lifecycleScope.launchWhenStarted {
                            viewModel.uid.collect {
                                sharedPrefs.edit()
                                    .putString(Constants.KEY_UID, it)
                                    .apply()
                            }
                        }

                        Intent(requireContext(), MainActivity::class.java).also {
                            startActivity(it)
                            requireActivity().finish()
                        }

                    }

                    is Resource.Error -> {
                        binding.loginProgressBar.isVisible = false

                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.loginProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }
        }

    }

    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email = email
        basicAuthInterceptor.password = password
    }


}