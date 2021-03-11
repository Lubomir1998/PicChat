package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.User
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel
@Inject constructor(private val repository: MainRepository): ViewModel() {

    private val _comments = MutableStateFlow<Event<Resource<List<Comment>>>>(Event((Resource.Empty())))

    val comments: StateFlow<Event<Resource<List<Comment>>>> = _comments

    fun getComments(postId: String) {
        _comments.value = Event(Resource.Loading())

        val commentsFlow = flow {
            emit(repository.getComments(postId))
        }

        viewModelScope.launch {
            commentsFlow.collect {
                _comments.value = Event(it)
            }
        }
    }


    private val _addCommentState = MutableStateFlow<Event<Resource<String?>>>(Event((Resource.Empty())))

    val addCommentState: StateFlow<Event<Resource<String?>>> = _addCommentState

    fun comment(text: String, postId: String) {
        if(text.trim().isEmpty()) {
            _addCommentState.value = Event(Resource.Error("Field is empty"))
            return
        }
        _addCommentState.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.addComment(text, postId)
            _addCommentState.value = Event(result)
        }
    }


    private val _deleteCommentState = MutableStateFlow<Event<Resource<String?>>>(Event((Resource.Empty())))

    val deleteCommentState: StateFlow<Event<Resource<String?>>> = _deleteCommentState

    fun deleteComment(comment: Comment) {
        _deleteCommentState.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.deleteComment(comment)
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

    private val _tokensState = MutableStateFlow<Resource<List<String>>>(Resource.Empty())
    val tokensState: StateFlow<Resource<List<String>>> = _tokensState

    fun getTokens(uid: String) {
        viewModelScope.launch {
            val tokens = repository.getTokens(uid)
            _tokensState.value = tokens
        }
    }


}