package com.example.picchat.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.picchat.other.Constants.DEFAULT_POSITION_VALUE
import com.example.picchat.other.Constants.KEY_POSITION
import com.example.picchat.viewmodels.BasePostViewModel
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: BasePostFragment() {

    override val position: Int
        get() {
            return if (sharedPrefs.getInt(KEY_POSITION, DEFAULT_POSITION_VALUE) == DEFAULT_POSITION_VALUE) {
                0
            }
            else sharedPrefs.getInt(KEY_POSITION, DEFAULT_POSITION_VALUE)
        }
    override var uid: String = ""
        get() = ""
    override val viewModel: BasePostViewModel
        get() {
            val vm: HomeViewModel by viewModels()
            return vm
        }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPosts()



        binding.swipeRefreshHome.setOnRefreshListener {
            viewModel.getPosts()
            binding.swipeRefreshHome.isRefreshing = false
        }


        postAdapter.setOnUsernameClickListener { uid, pos ->
            findNavController().navigate(
                    HomeFragmentDirections.launchOthersProfileFragment(uid)
            )
        }





    }

    override fun getPosts(uid: String) {
        viewModel.getPosts()
    }




}