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
import com.example.picchat.adapters.NotificationsAdapter
import com.example.picchat.data.NotificationData
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Notification
import com.example.picchat.databinding.NotificationsFragmentBinding
import com.example.picchat.other.Constants
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment: Fragment() {

    private lateinit var binding: NotificationsFragmentBinding

    private val viewModel: NotificationViewModel by viewModels()

    private var position = 0
    private var uid = "uid"

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var notificationAdapter: NotificationsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = NotificationsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()

        viewModel.getNotifications()

        binding.notificationsSwipeRefresh.setOnRefreshListener {
            viewModel.getNotifications()
            binding.notificationsSwipeRefresh.isRefreshing = false
        }


        collectNotificationsData()

        notificationAdapter.setOnProfileImgClickListener {
            findNavController().navigate(
                NotificationsFragmentDirections.launchOthersProfileFragment(it)
            )
        }

        notificationAdapter.setOnPostClickListener { postId ->
            findNavController().navigate(
                    NotificationsFragmentDirections.actionNotificationsFragmentToPostItemFragment(postId)
            )
        }

        notificationAdapter.setOnBtnFollowClickListener { uid, pos ->
            position = pos
            viewModel.toggleFollow(uid)
        }

        collectFollowStateData()

    }



    private fun collectNotificationsData() {
        lifecycleScope.launchWhenStarted {
            viewModel.notifications.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.notificationsProgressbar.isVisible = false

                        val notifications = result.data!!.reversed()

                        notificationAdapter.submitList(notifications)

                    }

                    is Resource.Error -> {
                        binding.notificationsProgressbar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }
                        }
                    }

                    is Resource.Loading -> { binding.notificationsProgressbar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }
        }
    }


    private fun collectFollowStateData() {
        lifecycleScope.launchWhenStarted {
            viewModel.toggleFollowState.collect {
                when (val result = it.peekContent()) {
                    is Resource.Success -> {
                        val notification = notificationAdapter.currentList[position]

                        notification.isFollowing = result.data!!
                        val username = sharedPrefs.getString(Constants.KEY_USERNAME, "Someone") ?: "Someone"
                        if(notification.isFollowing) {
                            viewModel.sendPushNotification(
                                PushNotification(
                                    NotificationData(
                                        username,
                                        Constants.FOLLOW_MESSAGE
                                    ), "/topics/${notification.senderUid}"
                                )
                            )
                        }

                        notificationAdapter.notifyItemChanged(position)

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


    private fun setupRecyclerView() {
        binding.recyclerViewNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}