package com.example.picchat.ui.main

import android.content.SharedPreferences
import android.os.Bundle
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
import com.example.picchat.databinding.SearchFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.search_fragment) {

    private lateinit var binding: SearchFragmentBinding
    private val viewModel: SearchViewModel by viewModels()

    @Inject
    lateinit var searchAdapter: SearchUserAdapter

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


        searchAdapter.setOnUserClicked {
            findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToOthersProfileFragment(it.uid)
            )
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