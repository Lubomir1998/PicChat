package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.entities.Post
import com.example.picchat.data.entities.User
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BasePostViewModel(private val repository: MainRepository): ViewModel() {

    abstract val posts: StateFlow<Event<Resource<List<Post>>>>

    abstract fun getPosts(uid: String = "")

    private val _isLikedState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Empty()))
    val isLikedState: StateFlow<Event<Resource<Boolean>>> = _isLikedState

    fun toggleLike(postId: String) {
        _isLikedState.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.toggleLike(postId)
            _isLikedState.value = Event(result)
        }
    }


}