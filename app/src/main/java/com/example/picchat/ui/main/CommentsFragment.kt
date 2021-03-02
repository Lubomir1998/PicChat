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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.R
import com.example.picchat.adapters.CommentAdapter
import com.example.picchat.databinding.CommentsFragmentBinding
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.CommentsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class CommentsFragment: Fragment(R.layout.comments_fragment) {

    private lateinit var binding: CommentsFragmentBinding
    private val args: CommentsFragmentArgs by navArgs()
    private val viewModel: CommentsViewModel by viewModels()

    @Inject
    lateinit var commentsAdapter: CommentAdapter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CommentsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId

        val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

        setupRecyclerView()

        binding.btnPostComment.isEnabled = false

        binding.etComment.addTextChangedListener { editable ->
            editable?.let {
                if(it.isNotEmpty()) {
                    binding.btnPostComment.isEnabled = true
                }
            }
        }

        binding.btnPostComment.setOnClickListener {
            viewModel.comment(binding.etComment.text.toString(), postId)
        }

        commentsAdapter.setOnUsernameClickListener {
            if(currentUid != it) {
                findNavController().navigate(
                        CommentsFragmentDirections.actionCommentsFragmentToOthersProfileFragment(
                                it
                        )
                )
            }
            else {
                findNavController().navigate(
                    CommentsFragmentDirections.actionCommentsFragmentToProfileFragment()
                )
            }
        }


        viewModel.getComments(postId)


        lifecycleScope.launchWhenStarted {
            viewModel.comments.collect {
                when(val result = it.peekContent()) {
                    is Resource.Success -> {
                        binding.commentsProgressBar.isVisible = false

                        val comments = result.data!!

                        commentsAdapter.submitList(comments)
                    }

                    is Resource.Error -> {
                        binding.commentsProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.commentsProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }

        }

        lifecycleScope.launchWhenStarted {
            viewModel.addCommentState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        binding.btnPostComment.isEnabled = false
                        binding.commentsProgressBar.isVisible = false
                        viewModel.getComments(postId)
                        binding.etComment.text.clear()
                    }

                    is Resource.Error -> {
                        binding.commentsProgressBar.isVisible = false
                        binding.btnPostComment.isEnabled = true
                        it.getContentIfNotHandled()?.let {
                            it.message?.let { message ->
                                snackbar(message)
                            }
                        }
                    }

                    is Resource.Loading -> {
                        binding.commentsProgressBar.isVisible = true
                        binding.btnPostComment.isEnabled = false
                    }

                    is Resource.Empty -> Unit
                }
            }
        }






    }

    private fun setupRecyclerView() {
        binding.recyclerViewComments.apply {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}