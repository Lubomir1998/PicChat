package com.example.picchat.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picchat.R
import com.example.picchat.adapters.CommentAdapter
import com.example.picchat.databinding.CommentsFragmentBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CommentsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId

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







    }

    private fun setupRecyclerView() {
        binding.recyclerViewComments.apply {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}