package com.example.picchat.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.picchat.other.Constants
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.BasePostViewModel
import com.example.picchat.viewmodels.PostItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PostItemFragment: BasePostFragment() {

    private val args: PostItemFragmentArgs by navArgs()

    override val position: Int
        get() = 0
    override val viewModel: BasePostViewModel
        get() {
            val vm: PostItemViewModel by viewModels()
            return vm
        }
    override var uid: String
        get() = ""
        set(value) {}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPrefs.getString(Constants.KEY_UID, Constants.NO_UID) ?: Constants.NO_UID

        getPosts(args.postId)

        binding.swipeRefreshHome.setOnRefreshListener {
            binding.swipeRefreshHome.isRefreshing = false
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialog().apply {
                setPositiveListener {
                    viewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }

        postAdapter.setOnUsernameClickListener { id, pos ->
            if(currentUid == id) {
                findNavController().navigate(
                        PostItemFragmentDirections.actionPostItemFragmentToProfileFragment()
                )
            }
            else {
                findNavController().navigate(
                        PostItemFragmentDirections.launchOthersProfileFragment(id)
                )
            }
        }



        collectDeletePostState()

    }

    private fun collectDeletePostState() {
        lifecycleScope.launchWhenStarted {
            viewModel.deletePostState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        binding.allPostsProgressBar.isVisible = false
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