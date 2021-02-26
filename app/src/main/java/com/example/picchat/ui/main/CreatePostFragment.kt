package com.example.picchat.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.databinding.CreatePostFragmentBinding
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.viewmodels.CreatePostViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CreatePostFragment: Fragment(R.layout.create_post_fragment) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: CreatePostFragmentBinding
    private val viewModel: CreatePostViewModel by viewModels()
    private lateinit var cropContent: ActivityResultLauncher<String>
    private var currentUri: Uri? = null
    private val cropActivityResultContract = object : ActivityResultContract<String, Uri?>() {

        override fun createIntent(context: Context, input: String?): Intent {
            return CropImage.activity()
                .setAspectRatio(20, 21)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cropContent = registerForActivityResult(cropActivityResultContract) {
            it?.let { uri ->
                viewModel.setImgUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreatePostFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetPostImage.setOnClickListener {
            cropContent.launch("image/*")
        }

        binding.ivPostImage.setOnClickListener {
            cropContent.launch("image/*")
        }

        binding.btnPost.setOnClickListener {
            currentUri?.let {
                viewModel.createPost(it, binding.etPostDescription.text.toString())
            } ?: snackbar("No image chosen")
        }

        lifecycleScope.launchWhenStarted {
            viewModel.imgUri.collect {
                it?.let { uri ->
                    currentUri = uri
                    glide.load(currentUri).into(binding.ivPostImage)
                    binding.btnSetPostImage.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.createPostStatus.collect {
                when(it.peekContent()) {

                    is Resource.Success -> {
                        binding.createPostProgressBar.visibility = View.GONE
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.createPostProgressBar.visibility = View.GONE
                        it.getContentIfNotHandled()?.let {
                            it.message?.let { message ->
                                snackbar(message)
                            }
                        }
                    }

                    is Resource.Loading -> { binding.createPostProgressBar.visibility = View.VISIBLE }

                    is Resource.Empty -> Unit
                }
            }

        }






    }

}