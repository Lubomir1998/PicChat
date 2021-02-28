package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.User
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel
@Inject constructor(private val repository: MainRepository): ViewModel() {

    private val _comments = MutableStateFlow<Event<Resource<List<Comment>>>>(Event((Resource.Empty())))

    val comments: StateFlow<Event<Resource<List<Comment>>>> = _comments

    fun getComments(postId: String) {
        _comments.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getComments(postId)
            _comments.value = Event(result)
        }
    }


    private val _addCommentState = MutableStateFlow<Event<Resource<String?>>>(Event((Resource.Empty())))

    val addCommentState: StateFlow<Event<Resource<String?>>> = _addCommentState

    fun comment(text: String, postId: String) {
        if(text.isEmpty()) {
            _addCommentState.value = Event(Resource.Error("Field is empty"))
            return
        }
        _addCommentState.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.addComment(text, postId)
            _addCommentState.value = Event(result)
        }
    }



}