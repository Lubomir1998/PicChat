package com.example.picchat.ui.main

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.picchat.data.entities.User
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class OthersProfileFragment: ProfileFragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val args: OthersProfileFragmentArgs by navArgs()

    override val uid: String
        get() = args.uid


    override var currentUser: User? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPreferences.getString(KEY_UID, NO_UID) ?: NO_UID

        viewModel.loadProfile(uid)

        collectUserData()



        lifecycleScope.launchWhenStarted {
            viewModel.userFlow.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        profileBinding.profilePostsProgressBar.isVisible = false
                        profileBinding.btnFollow.isVisible = true
                        currentUser = it.peekContent().data
                        checkIfUserIsFollowed(currentUid)
                    }
                    is Resource.Loading -> {
                        profileBinding.profilePostsProgressBar.isVisible = true
                        profileBinding.btnFollow.isVisible = false
                    }
                    else -> Unit
                }
            }
        }


    }


    @SuppressLint("SetTextI18n")
    private fun checkIfUserIsFollowed(currentUid: String) {
        currentUser?.let {
            if(it.followers.contains(currentUid)) {
                profileBinding.btnFollow.apply {
                    text = "Following"
                    setTextColor(Color.BLACK)
                    setBackgroundColor(Color.WHITE)
                }
            }
            else {
                profileBinding.btnFollow.apply {
                    text = "Follow"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#14B6FA"))
                }
            }
        }
    }

}