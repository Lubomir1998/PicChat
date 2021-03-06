package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.adapters.PostAdapter
import com.example.picchat.data.NotificationData
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Notification
import com.example.picchat.databinding.HomeFragmentBinding
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.KEY_POSITION
import com.example.picchat.other.Constants.KEY_USERNAME
import com.example.picchat.other.Constants.LIKE_MESSAGE
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.BasePostViewModel
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

abstract class BasePostFragment(): Fragment() {

    protected lateinit var binding: HomeFragmentBinding
    private var index = 0

    abstract val position: Int

    abstract val viewModel: BasePostViewModel

    abstract var uid: String

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

        postAdapter.setOnLikesClickListener { post, pos ->
            sharedPrefs.edit().putInt(KEY_POSITION, pos).apply()
            findNavController().navigate(
                    HomeFragmentDirections.launchUserResultsFragment(
                            post.id,
                            "Likes"
                    )
            )
        }

        postAdapter.setOnCommentTvClickListener { post, pos ->
            sharedPrefs.edit().putInt(KEY_POSITION, pos).apply()
            findNavController().navigate(
                    HomeFragmentDirections.launchCommentsFragment(post.id, uid = post.authorUid, postImgUrl = post.imgUrl)
            )
        }

        postAdapter.setOnLikeBtnClickListener { post, i ->
            index = i
            viewModel.toggleLike(post.id)
        }

        collectPosts()

        collectIsLikedState()

        collectAddNotificationState()

    }


    private fun collectPosts() {
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.allPostsProgressBar.isVisible = false

                        val posts = result.data!!.reversed()

                        postAdapter.submitList(posts)
                        binding.recyclerViewHome.scrollToPosition(position)
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


    private fun collectIsLikedState() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLikedState.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val post = postAdapter.currentList[index]

                        val currentUid = sharedPrefs.getString(Constants.KEY_UID, Constants.NO_UID) ?: Constants.NO_UID

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
                                        viewModel.sendPushNotification(PushNotification(NotificationData(username, LIKE_MESSAGE), token))
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

    private fun setUpRecyclerView() {
        binding.recyclerViewHome.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            animation = null
            setHasFixedSize(true)
        }
    }

    abstract fun getPosts(uid: String = "")

}