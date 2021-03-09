package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.Post
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

    private val _deletePostState = MutableStateFlow<Event<Resource<String?>>>(Event((Resource.Empty())))

    val deletePostState: StateFlow<Event<Resource<String?>>> = _deletePostState

    fun deletePost(post: Post) {
        _deletePostState.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.deletePost(post)
            Event(result)
        }
    }

    private val _addNotificationState = MutableStateFlow<Event<Resource<String?>>>(Event(Resource.Loading()))
    val addNotificationState: StateFlow<Event<Resource<String?>>> = _addNotificationState

    fun addNotification(notification: Notification) {
        viewModelScope.launch {
            val result = repository.addNotification(notification)
            _addNotificationState.value = Event(result)
        }
    }


    fun sendPushNotification(pushNotification: PushNotification) = viewModelScope.launch {
        repository.sendPushNotification(pushNotification)
    }




}