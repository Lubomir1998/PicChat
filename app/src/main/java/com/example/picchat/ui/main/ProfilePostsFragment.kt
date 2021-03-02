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
import androidx.viewpager2.widget.ViewPager2
import com.example.picchat.adapters.PostAdapter
import com.example.picchat.databinding.ProfilePostsFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ProfilePostsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = sharedPreferences.getString(KEY_UID, NO_UID) ?: NO_UID

        val uid = args.uid
        val position = args.position
                .also {
                    sharedPreferences.edit().putInt("pos", it).apply()
                }

//        if(position == 999999) {
//            position = sharedPreferences.getInt("pos", 0)
//        }
//
//        if(uid == "qqqqqq") {
//            uid = sharedPreferences.getString("id", NO_UID) ?: NO_UID
//        }


        setupViewPager()

        viewModel.getPosts(uid)

        lifecycleScope.launchWhenStarted {
            viewModel.posts.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        val posts = result.data!!.reversed()
                        postAdapter.submitList(posts)
                        binding.viewPagerProfilePosts.currentItem = if(sharedPreferences.getInt("pos2", 999999) != 999999) {
                            sharedPreferences.getInt("pos2", 999999)
                        }
                        else {
                            position
                        }
                    }
                }
            }
        }

        postAdapter.setOnCommentTvClickListener { post, pos ->
            sharedPreferences.edit().putInt("pos2", pos).apply()
            sharedPreferences.edit().putString("id", uid).apply()

            findNavController().navigate(
                    ProfilePostsFragmentDirections.launchCommentsFragment(post.id)
            )
        }

        postAdapter.setOnUsernameClickListener { id, pos ->
            sharedPreferences.edit().putInt("pos", pos).apply()
            sharedPreferences.edit().putString("id", uid).apply()

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

    }


    private fun setupViewPager() {
        binding.viewPagerProfilePosts.apply {
            adapter = postAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
        }
    }

}