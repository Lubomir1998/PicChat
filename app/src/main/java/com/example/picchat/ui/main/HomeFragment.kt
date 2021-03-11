package com.example.picchat.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.picchat.viewmodels.BasePostViewModel
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: BasePostFragment() {

    override val position: Int
        get() = 0
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


        postAdapter.setOnUsernameClickListener { uid, _ ->
            findNavController().navigate(
                    HomeFragmentDirections.launchOthersProfileFragment(uid)
            )
        }





    }

    override fun getPosts(uid: String) {
        viewModel.getPosts()
    }




}