package com.example.picchat.ui.main

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.databinding.ProfileFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
open class ProfileFragment: Fragment(R.layout.profile_fragment) {

    private lateinit var binding: ProfileFragmentBinding
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var glide: RequestManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditProfile.visibility = View.VISIBLE
        binding.btnFollow.visibility = View.GONE

        val uid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

        binding.swipeRefreshProfile.setOnRefreshListener {
            if(uid != NO_UID) {
                viewModel.loadProfile(uid)
            }
            binding.swipeRefreshProfile.isRefreshing = false
        }



        if(uid != NO_UID) {
            viewModel.loadProfile(uid)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userFlow.collect {
                when(val result = it.peekContent()) {

                    is Resource.Success -> {
                        binding.profileProgressBar.isVisible = false
                        val user = result.data!!

                        glide.load(user.profileImgUrl).into(binding.circleImageView)

                        if(user.description.trim().isEmpty()) {
                            binding.profileBioTv.visibility = View.GONE
                        }
                        else {
                            binding.profileBioTv.visibility = View.VISIBLE
                            binding.profileBioTv.text = user.description.trim()
                        }

                        binding.profileUsernameTv.text = user.username
                        binding.postsTv.text = "${user.posts}\nposts"
                        binding.followersTv.text = "${user.followers.size}\nfollowers"
                        binding.followingTv.text = "${user.followers.size}\nfollowing"


                    }

                    is Resource.Error -> {
                        binding.profileProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.profileProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }
        }


    }

}