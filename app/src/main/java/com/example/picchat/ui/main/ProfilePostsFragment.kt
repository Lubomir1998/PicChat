package com.example.picchat.ui.main

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.adapters.PostAdapter
import com.example.picchat.data.NotificationData
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Notification
import com.example.picchat.databinding.HomeFragmentBinding
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.LIKE_MESSAGE
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.BasePostViewModel
import com.example.picchat.viewmodels.HomeViewModel
import com.example.picchat.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ProfilePostsFragment: BasePostFragment() {


    private val args: ProfilePostsFragmentArgs by navArgs()

    override var uid: String = ""
        get() = args.uid

    override val position: Int
        get() = args.position
    override val viewModel: BasePostViewModel
        get() {
            val vm: ProfileViewModel by viewModels()
            return vm
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID


        getPosts(uid)

        binding.swipeRefreshHome.setOnRefreshListener {
            viewModel.getPosts(uid)
            binding.swipeRefreshHome.isRefreshing = false
        }


        postAdapter.setOnUsernameClickListener { id, pos ->
            if(currentUid == id) {
                findNavController().navigate(
                        ProfilePostsFragmentDirections.actionProfilePostsFragmentToProfileFragment()
                )
            }
            else {
                findNavController().navigate(
                        ProfilePostsFragmentDirections.launchOthersProfileFragment(id)
                )
            }
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialog().apply {
                setPositiveListener {
                    viewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }



        collectDeletePostState()

    }


    private fun collectDeletePostState() {
        lifecycleScope.launchWhenStarted {
            viewModel.deletePostState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        binding.allPostsProgressBar.isVisible = false
                        viewModel.getPosts(args.uid)
                    }

                    is Resource.Error -> {
                        binding.allPostsProgressBar.isVisible = false
                        snackbar(it.getContentIfNotHandled()?.message ?: "Something went wrong")
                    }

                    is Resource.Loading -> {
                        binding.allPostsProgressBar.isVisible = true
                    }

                    is Resource.Empty -> Unit
                }
            }
        }
    }


    override fun getPosts(uid: String) {
        viewModel.getPosts(uid)
    }



}