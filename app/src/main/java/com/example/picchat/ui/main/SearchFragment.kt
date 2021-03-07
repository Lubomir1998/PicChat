package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.R
import com.example.picchat.adapters.SearchUserAdapter
import com.example.picchat.data.NotificationData
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.User
import com.example.picchat.databinding.SearchFragmentBinding
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.FOLLOW_MESSAGE
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.search_fragment) {

    private lateinit var binding: SearchFragmentBinding
    private val viewModel: SearchViewModel by viewModels()

    private var position = 0

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var searchAdapter: SearchUserAdapter

    private var uid = "uid"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        var job: Job? = null

        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(500)
                editable?.let {
                    viewModel.searchUsers(it.toString())
                }

            }
        }



        searchAdapter.setOnBtnFollowClickListener { uid, pos ->
            position = pos
            viewModel.toggleFollow(uid)
        }

        searchAdapter.setOnUserClicked {
            findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToOthersProfileFragment(it.uid)
            )
        }

        collectUserStateFlow()

        collectFollowStateData()

        collectAddNotificationState()


    }


    private fun collectUserStateFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.users.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.searchProgressBar.isVisible = false

                        val users = result.data!!
                        searchAdapter.submitList(users)
                    }

                    is Resource.Error -> {
                        binding.searchProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.searchProgressBar.isVisible = true }

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
                        val user = searchAdapter.currentList[position]
                        uid = user.uid

                        val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

                        user.apply {

                            isFollowing = result.data!!

                            if(isFollowing) {
                                followers += currentUid
                                viewModel.addNotification(
                                    Notification(
                                        currentUid,
                                        uid,
                                        FOLLOW_MESSAGE
                                    )
                                )
                            }
                            else {
                                followers -= currentUid
                            }
                        }

                        searchAdapter.notifyItemChanged(position)

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



    private fun collectAddNotificationState() {
        lifecycleScope.launchWhenStarted {
            viewModel.addNotificationState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        val username = sharedPrefs.getString(Constants.KEY_USERNAME, "Someone") ?: "Someone"
                        viewModel.sendPushNotification(PushNotification(NotificationData(username, FOLLOW_MESSAGE), "/topics/$uid"))
                    }
                    else -> Unit
                }
            }
        }
    }


    private fun setupRecyclerView() {
        binding.recyclerViewSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}