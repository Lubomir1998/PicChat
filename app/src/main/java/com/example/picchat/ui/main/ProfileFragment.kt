package com.example.picchat.ui.main

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.adapters.ProfilePostsAdapter
import com.example.picchat.data.entities.User
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

    lateinit var profileBinding: ProfileFragmentBinding

    protected open var currentUser: User? = null

    protected open val viewModel: ProfileViewModel
        get() {
            val vm: ProfileViewModel by viewModels()
            return vm
        }

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var profilePostAdapter: ProfilePostsAdapter

    @Inject
    lateinit var glide: RequestManager

    protected open val uid: String
        get() = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileBinding = ProfileFragmentBinding.inflate(inflater, container, false)
        return profileBinding.root
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileBinding.btnFollow.apply {
            text = "Edit profile"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
        }


        setupRecyclerView()

        profileBinding.btnFollow.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(
                            currentUser?.profileImgUrl ?: "",
                            currentUser?.username ?: "",
                            currentUser?.description ?: ""
                    )
            )
        }

        profileBinding.followingTv.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.launchUserResultsFragment(currentUser!!.uid, "Following")
            )
        }

        profileBinding.followersTv.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.launchUserResultsFragment(currentUser!!.uid, "Followers")
            )
        }


        profileBinding.swipeRefreshProfile.setOnRefreshListener {
            if(uid != NO_UID) {
                viewModel.loadProfile(uid)
            }
            profileBinding.swipeRefreshProfile.isRefreshing = false
        }

        profilePostAdapter.setOnPostClickListener { authorUid, position ->
            findNavController().navigate(
                    ProfileFragmentDirections.launchProfilePostsFragment(
                            authorUid,
                            position
                    )
            )
        }



        viewModel.loadProfile(uid)

        collectUserData()


    }


    @SuppressLint("SetTextI18n")
    fun collectUserData() {
        lifecycleScope.launchWhenStarted {
            viewModel.userFlow.collect {
                when(val result = it.peekContent()) {

                    is Resource.Success -> {
                        profileBinding.btnFollow.isVisible = true
                        profileBinding.profileProgressBar.isVisible = false
                        currentUser = result.data


                        viewModel.getPosts(uid)

                        glide.load(currentUser!!.profileImgUrl).into(profileBinding.circleImageView)

                        if(currentUser!!.description.trim().isEmpty()) {
                            profileBinding.profileBioTv.visibility = View.GONE
                        }
                        else {
                            profileBinding.profileBioTv.visibility = View.VISIBLE
                            profileBinding.profileBioTv.text = currentUser!!.description.trim()
                        }

                        profileBinding.profileUsernameTv.text = currentUser!!.username
                        profileBinding.postsTv.text = "${currentUser!!.posts}\nposts"
                        profileBinding.followersTv.text = "${currentUser!!.followers.size}\nfollowers"
                        profileBinding.followingTv.text = "${currentUser!!.following.size}\nfollowing"

                        viewModel.posts.collect {
                            when(it.peekContent()) {
                                is Resource.Success -> {
                                    profileBinding.btnFollow.isVisible = true
                                    profileBinding.profilePostsProgressBar.isVisible = false
                                    val posts = it.peekContent().data!!.reversed()
                                    profilePostAdapter.submitList(posts)
                                }

                                is Resource.Error -> {
                                    profileBinding.btnFollow.isVisible = true
                                    profileBinding.profilePostsProgressBar.isVisible = false
                                    it.getContentIfNotHandled()?.let { error ->
                                        error.message?.let { message ->
                                            snackbar(message)
                                        }

                                    }
                                }

                                is Resource.Loading -> {
                                    profileBinding.profilePostsProgressBar.isVisible = true
                                    profileBinding.btnFollow.isVisible = false
                                }

                                is Resource.Empty -> Unit
                            }
                        }

                    }

                    is Resource.Error -> {
                        profileBinding.profileProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { profileBinding.profileProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }

        }
    }


    private fun setupRecyclerView() {
        profileBinding.recyclerViewProfilePosts.apply {
            adapter = profilePostAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            animation = null
        }
    }

}