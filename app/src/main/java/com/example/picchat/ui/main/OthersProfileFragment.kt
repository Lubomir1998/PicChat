package com.example.picchat.ui.main

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.picchat.data.NotificationData
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.User
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.FOLLOW_MESSAGE
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.KEY_USERNAME
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


    @RequiresApi(Build.VERSION_CODES.M)
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
                        profileBinding.btnFollow.isEnabled = true
                        currentUser?.let { user ->
                            user.apply {
                                isFollowing = result.data!!

                                if(isFollowing) {
                                    followers += currentUid
                                    viewModel.addNotification(
                                        Notification(
                                            currentUid,
                                            uid,
                                            FOLLOW_MESSAGE
                                        )
                                    )
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

        collectAddNotificationState()

    }

    private fun collectAddNotificationState() {
        lifecycleScope.launchWhenStarted {
            viewModel.addNotificationState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        viewModel.getTokens(uid)

                        viewModel.tokensState.collect{
                            when(it) {
                                is Resource.Success -> {
                                    val username = sharedPrefs.getString(KEY_USERNAME, "Someone") ?: "Someone"
                                    val tokens = it.data!!

                                    tokens.forEach { token ->
                                        viewModel.sendPushNotification(PushNotification(NotificationData(username, FOLLOW_MESSAGE), token))
                                    }

                                }

                                else -> Unit
                            }
                        }
                    }
                    else -> Unit
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