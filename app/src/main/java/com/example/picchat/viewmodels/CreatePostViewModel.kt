package com.example.picchat.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.entities.Post
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel
@Inject constructor(private val repository: MainRepository): ViewModel() {


    private val _createPostStatus = MutableStateFlow<Event<Resource<String?>>>(Event(Resource.Empty()))

    val createPostStatus: StateFlow<Event<Resource<String?>>> = _createPostStatus

    fun createPost(imageUri: Uri, text: String) {
        _createPostStatus.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.createPost(imageUri, text)
            _createPostStatus.value = Event(result)
        }
    }



    private val _imgUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val imgUri: StateFlow<Uri?> = _imgUri

    fun setImgUri(uri: Uri) {
        _imgUri.value = uri
    }

}