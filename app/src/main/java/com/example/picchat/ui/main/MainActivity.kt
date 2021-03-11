package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.picchat.R
import com.example.picchat.databinding.ActivityMainBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setupWithNavController(supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)!!.findNavController())

        supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)!!.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                 R.id.createPostFragment, R.id.commentsFragment, R.id.followFollowingUsersFragment ->
                    binding.bottomNav.visibility = View.GONE
                else ->
                    binding.bottomNav.visibility = View.VISIBLE
            }
        }


        navController = Navigation.findNavController(this, R.id.main_nav_host_fragment)
        setupWithNavController(
                findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar),
                navController
        )

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.createPostFragment -> toolbar.visibility = View.GONE
                else -> toolbar.visibility = View.VISIBLE
            }
        }

    }
}