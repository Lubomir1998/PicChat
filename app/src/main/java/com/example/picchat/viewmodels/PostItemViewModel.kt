package com.example.picchat.viewmodels

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
class PostItemViewModel
@Inject constructor(private val repository: MainRepository): BasePostViewModel(repository) {

    private val _posts = MutableStateFlow<Event<Resource<List<Post>>>>(Event(Resource.Loading()))

    override val posts: StateFlow<Event<Resource<List<Post>>>>
        get() = _posts

    override fun getPosts(uid: String) {
        viewModelScope.launch {
            val result = repository.getPostById(uid)
            _posts.value = Event(result)
        }
    }
}