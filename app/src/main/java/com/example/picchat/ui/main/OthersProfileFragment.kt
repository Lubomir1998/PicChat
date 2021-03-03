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
import com.example.picchat.other.snackbar
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


    @SuppressLint("SetTextI18n")
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
                        checkIfUserIsFollowed(currentUser, currentUid)
                    }
                    is Resource.Loading -> {
                        profileBinding.profilePostsProgressBar.isVisible = true
                        profileBinding.btnFollow.isVisible = false
                    }
                    else -> Unit
                }
            }
        }


        profileBinding.btnFollow.setOnClickListener {
            viewModel.toggleFollow(uid)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.toggleFollowState.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        currentUser?.let {
                            it.apply {
                                isFollowing = result.data!!

                                if(isFollowing) {
                                    followers += currentUid
                                }
                                else {
                                    followers -= currentUid
                                }
                            }
                            checkIfUserIsFollowed(currentUser, currentUid)

                            profileBinding.apply {
                                profileUsernameTv.text = currentUser!!.username
                                postsTv.text = "${currentUser!!.posts}\nposts"
                                followersTv.text = "${currentUser!!.followers.size}\nfollowers"
                                followingTv.text = "${currentUser!!.following.size}\nfollowing"
                            }
                        }



                    }

                    is Resource.Error -> {
                        profileBinding.btnFollow.isEnabled = true
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }
                        }
                    }

                    is Resource.Loading -> { profileBinding.btnFollow.isEnabled = false }

                    is Resource.Empty -> Unit
                }
            }
        }


    }


    @SuppressLint("SetTextI18n")
    private fun checkIfUserIsFollowed(currentUser: User?, currentUid: String) {
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