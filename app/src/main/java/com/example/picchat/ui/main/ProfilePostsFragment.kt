package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.picchat.adapters.PostAdapter
import com.example.picchat.databinding.ProfilePostsFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ProfilePostsFragment: Fragment() {

    private lateinit var binding: ProfilePostsFragmentBinding

    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var postAdapter: PostAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val args: ProfilePostsFragmentArgs by navArgs()

    private var index = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ProfilePostsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPreferences.getString(KEY_UID, NO_UID) ?: NO_UID

        val uid = args.uid
        val position = args.position

        setupViewPager()

        viewModel.getPosts(uid)

        lifecycleScope.launchWhenStarted {
            viewModel.posts.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val posts = result.data!!.reversed()
                        postAdapter.submitList(posts)
                        binding.viewPagerProfilePosts.currentItem = position
                    }
                }
            }
        }

        postAdapter.setOnLikeBtnClickListener { post, i ->
            index = i
            viewModel.toggleLike(post.id)
        }

        postAdapter.setOnCommentTvClickListener { post, pos ->
            findNavController().navigate(
                    ProfilePostsFragmentDirections.launchCommentsFragment(
                            post.id,
                            pos,
                            "post",
                            uid
                    )
            )
        }

        postAdapter.setOnLikesClickListener {
            findNavController().navigate(
                    ProfilePostsFragmentDirections.launchUserResultsFragment(
                            it.id,
                            "Likes"
                    )
            )
        }

        postAdapter.setOnUsernameClickListener { id, pos ->
            if(currentUid == id) {
                findNavController().navigate(
                        ProfilePostsFragmentDirections.actionProfilePostsFragmentToProfileFragment()
                )
            }
            else {
                findNavController().navigate(
                        ProfilePostsFragmentDirections.actionProfilePostsFragmentToOthersProfileFragment(id)
                )
            }
        }

        collectIsLikedState()

    }

    private fun collectIsLikedState() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLikedState.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val post = postAdapter.currentList[index]

                        val currentUid = sharedPreferences.getString(KEY_UID, NO_UID) ?: NO_UID

                        post.apply {
                            isLiking = false
                            isLiked = result.data!!
                            if(isLiked) {
                                likes += currentUid
                            }
                            else {
                                likes -= currentUid
                            }
                        }

                        postAdapter.notifyItemChanged(index)

                    }

                    is Resource.Error -> {
                        val post = postAdapter.currentList[index]
                        post.isLiking = false
                        snackbar(it.getContentIfNotHandled()?.message ?: "Something went wrong")
                    }

                    is Resource.Loading -> {
                        val post = postAdapter.currentList[index]
                        post.isLiking = true
                    }

                    is Resource.Empty -> Unit
                }
            }
        }
    }


    private fun setupViewPager() {
        binding.viewPagerProfilePosts.apply {
            adapter = postAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
        }
    }

}