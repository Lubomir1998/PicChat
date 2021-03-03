package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.adapters.SearchUserAdapter
import com.example.picchat.databinding.FollowFollowingUsersFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class FollowFollowingUsersFragment: Fragment() {

    private lateinit var binding: FollowFollowingUsersFragmentBinding
    private val args: FollowFollowingUsersFragmentArgs by navArgs()

    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var usersAdapter: SearchUserAdapter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FollowFollowingUsersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

        val uid = args.uid
        val request = args.request

        setUpRecyclerView()

        when (request) {
            "Followers" -> {
                viewModel.getFollowers(uid)
            }
            "Following" -> {
                viewModel.getFollowing(uid)
            }
            "Likes" -> {
                viewModel.getLikes(uid)
            }
        }

        usersAdapter.setOnUserClicked {
            if(it.uid == currentUid) {
                findNavController().navigate(
                        FollowFollowingUsersFragmentDirections.actionFollowFollowingUsersFragmentToProfileFragment()
                )
            }
            else {
                findNavController().navigate(
                        FollowFollowingUsersFragmentDirections.actionFollowFollowingUsersFragmentToOthersProfileFragment(it.uid)
                )
            }
        }

        usersAdapter.setOnBtnFollowClickListener { id, pos ->
            viewModel.toggleFollow(id)
        }




        collectToggleFollowStateFlow(uid, request)

        collectFollowsStateFlow()

        collectLikes()

    }


    private fun collectLikes() {
        lifecycleScope.launchWhenStarted {

            viewModel.likes.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val likes = result.data!!
                        usersAdapter.submitList(likes)
                    }

                    is Resource.Error -> {
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { }

                    is Resource.Empty -> Unit

                }
            }
        }
    }

    private fun collectToggleFollowStateFlow(uid: String, request: String) {
        lifecycleScope.launchWhenStarted {
            viewModel.toggleFollowState.collect {
                when (it.peekContent()) {
                    is Resource.Success -> {
                        when (request) {
                            "Followers" -> {
                                viewModel.getFollowers(uid)
                            }
                            "Following" -> {
                                viewModel.getFollowing(uid)
                            }
                            "Likes" -> {
                                viewModel.getLikes(uid)
                            }
                        }
                    }

                    is Resource.Error -> {
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }
                        }
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Empty -> Unit
                }
            }
        }
    }

    private fun collectFollowsStateFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.followers.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val followers = result.data!!
                        usersAdapter.submitList(followers)
                    }

                    is Resource.Error -> {
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { }

                    is Resource.Empty -> Unit

                }
            }

            viewModel.following.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val following = result.data!!
                        usersAdapter.submitList(following)
                    }

                    is Resource.Error -> {
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { }

                    is Resource.Empty -> Unit

                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.rvUsers.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}