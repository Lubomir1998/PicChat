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
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private lateinit var binding: HomeFragmentBinding

    private val viewModel: HomeViewModel by viewModels()

    private var position = 0

    private var uid = "uid"

    @Inject
    lateinit var postAdapter: PostAdapter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        viewModel.getPosts()

        binding.swipeRefreshHome.setOnRefreshListener {
            viewModel.getPosts()
            binding.swipeRefreshHome.isRefreshing = false
        }


        postAdapter.setOnUsernameClickListener { uid, _ ->
            findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToOthersProfileFragment(uid)
            )
        }

        postAdapter.setOnCommentTvClickListener { post, _ ->
            findNavController().navigate(
                    HomeFragmentDirections.launchCommentsFragment(post.id, uid = post.authorUid, postImgUrl = post.imgUrl)
            )
        }

        postAdapter.setOnLikeBtnClickListener { post, index ->
            position = index
            viewModel.toggleLike(post.id)
        }

        postAdapter.setOnLikesClickListener {
            findNavController().navigate(
                    HomeFragmentDirections.launchUserResultsFragment(
                            it.id,
                            "Likes"
                    )
            )
        }

        collectHomePosts()
        collectIsLikedState()

        collectAddNotificationState()


    }


    private fun collectIsLikedState() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLikedState.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val post = postAdapter.currentList[position]

                        val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

                        post.apply {
                            uid = authorUid
                            isLiking = false
                            isLiked = result.data!!
                            if(isLiked) {
                                likes += currentUid
                                viewModel.addNotification(
                                    Notification(
                                        currentUid,
                                        authorUid,
                                        LIKE_MESSAGE,
                                        id,
                                        imgUrl
                                    )
                                )
                            }
                            else {
                                likes -= currentUid
                            }
                        }

                        postAdapter.notifyItemChanged(position)

                    }

                    is Resource.Error -> {
                        val post = postAdapter.currentList[position]
                        post.isLiking = false
                        snackbar(it.getContentIfNotHandled()?.message ?: "Something went wrong")
                    }

                    is Resource.Loading -> {
                        val post = postAdapter.currentList[position]
                        post.isLiking = true
                    }

                    is Resource.Empty -> Unit
                }
            }
        }
    }


    private fun collectAddNotificationState() {
        lifecycleScope.launchWhenStarted {
            viewModel.addNotificationState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        val username = sharedPrefs.getString(Constants.KEY_USERNAME, "Someone") ?: "Someone"
                        viewModel.sendPushNotification(PushNotification(NotificationData(username, LIKE_MESSAGE), "/topics/$uid"))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun collectHomePosts() {
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.allPostsProgressBar.isVisible = false

                        val posts = result.data!!.reversed()

                        postAdapter.submitList(posts)

                    }

                    is Resource.Error -> {
                        binding.allPostsProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.allPostsProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewHome.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            animation = null
            setHasFixedSize(true)
        }
    }



}