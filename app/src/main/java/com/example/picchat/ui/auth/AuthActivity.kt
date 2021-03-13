package com.example.picchat.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.picchat.R
import com.example.picchat.other.BasicAuthInterceptor
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.KEY_EMAIL
import com.example.picchat.other.Constants.KEY_PASSWORD
import com.example.picchat.other.Constants.NO_EMAIL
import com.example.picchat.other.Constants.NO_PASSWORD
import com.example.picchat.ui.main.MainActivity
import com.example.picchat.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PicChat)
        setContentView(R.layout.activity_auth)

        if(sharedPrefs.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        val email = sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL
        val password = sharedPrefs.getString(KEY_PASSWORD, NO_PASSWORD) ?: NO_PASSWORD

        if(email != NO_EMAIL && password != NO_PASSWORD) {

            basicAuthInterceptor.email = email
            basicAuthInterceptor.password = password

            authViewModel.getUid()

            lifecycleScope.launchWhenStarted {
                authViewModel.uid.collect {
                    sharedPrefs.edit()
                        .putString(Constants.KEY_UID, it)
                        .apply()
                }
            }

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

    }
}