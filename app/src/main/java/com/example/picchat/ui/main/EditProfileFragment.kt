package com.example.picchat.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.databinding.EditProfileFragmentBinding
import com.example.picchat.other.Constants
import com.example.picchat.other.Resource
import com.example.picchat.other.snackbar
import com.example.picchat.ui.auth.AuthActivity
import com.example.picchat.viewmodels.EditProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment: Fragment(R.layout.edit_profile_fragment) {

    private lateinit var binding: EditProfileFragmentBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private val args: EditProfileFragmentArgs by navArgs()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var sharedPrefs: SharedPreferences

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
                binding.btnDone.isEnabled = true
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = EditProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDone.isEnabled = false

        binding.changePhotoTv.setOnClickListener {
            cropContent.launch("image/*")
        }

        if(args.profileImgUrl.isNotEmpty()) {
            glide.load(args.profileImgUrl).into(binding.profileImg)
        }

        binding.etLoginEmail.apply {
            setText(args.username)
            addTextChangedListener {
                binding.btnDone.isEnabled = true
            }
        }

        binding.etBio.apply {
            setText(args.description)
            addTextChangedListener {
                binding.btnDone.isEnabled = true
            }
        }

        binding.btnDone.setOnClickListener {
            val username = binding.etLoginEmail.text.toString()
            val bio = binding.etBio.text.toString()

            viewModel.updateProfile(currentUri, username, bio)
        }


        binding.logOutTv.setOnClickListener {
            logout()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateProfileState.collect {
                when(it.peekContent()) {
                    is Resource.Success -> {
                        binding.updateProfileProgressBar.isVisible = false
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.updateProfileProgressBar.isVisible = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                snackbar(message)
                            }

                        }
                    }

                    is Resource.Loading -> { binding.updateProfileProgressBar.isVisible = true }

                    is Resource.Empty -> Unit
                }
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.imgUri.collect {
                it?.let { uri ->
                    currentUri = uri
                    glide.load(currentUri).into(binding.profileImg)
                }
            }
        }




    }

    private fun logout() {
        sharedPrefs.edit()
                .putString(Constants.KEY_UID, Constants.NO_UID)
                .putString(Constants.KEY_EMAIL, Constants.NO_EMAIL)
                .putString(Constants.KEY_PASSWORD, Constants.NO_PASSWORD)
                .apply()

        startActivity(Intent(requireContext(), AuthActivity::class.java))
        requireActivity().finish()
    }

}