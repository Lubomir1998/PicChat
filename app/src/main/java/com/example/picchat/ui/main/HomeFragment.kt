package com.example.picchat.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.R
import com.example.picchat.adapters.PostAdapter
import com.example.picchat.databinding.HomeFragmentBinding
import com.example.picchat.other.Constants.KEY_EMAIL
import com.example.picchat.other.Constants.KEY_PASSWORD
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_EMAIL
import com.example.picchat.other.Constants.NO_PASSWORD
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.ui.auth.AuthActivity
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding

    private val viewModel: HomeViewModel by viewModels()

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

        binding.swipeRefreshHome.setOnRefreshListener {
            viewModel.getPosts()
            binding.swipeRefreshHome.isRefreshing = false
        }

        setUpRecyclerView()

        postAdapter.setOnUsernameClickListener {
            findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToOthersProfileFragment(it)
            )
        }

        postAdapter.setOnCommentTvClickListener {
            findNavController().navigate(
                    HomeFragmentDirections.launchCommentsFragment(it.id)
            )
        }

        viewModel.getPosts()

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


    private fun logout() {
        sharedPrefs.edit()
            .putString(KEY_UID, NO_UID)
            .putString(KEY_EMAIL, NO_EMAIL)
            .putString(KEY_PASSWORD, NO_PASSWORD)
            .apply()

        startActivity(Intent(requireContext(), AuthActivity::class.java))
    }

}