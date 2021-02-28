package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.picchat.R
import com.example.picchat.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setupWithNavController(supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)!!.findNavController())

        supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)!!.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                 R.id.createPostFragment, R.id.notificationsFragment, R.id.commentsFragment ->
                    binding.bottomNav.visibility = View.GONE
                else ->
                    binding.bottomNav.visibility = View.VISIBLE
            }
        }

    }
}