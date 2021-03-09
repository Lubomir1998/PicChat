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
import com.example.picchat.viewmodels.BasePostViewModel
import com.example.picchat.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: BasePostFragment() {

    override val position: Int
        get() = 0
    override var uid: String = ""
        get() = "uid"
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