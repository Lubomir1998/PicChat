package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.data.entities.Post
import com.example.picchat.data.entities.User
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(private val repository: MainRepository): BasePostViewModel(repository) {

    private val _posts = MutableStateFlow<Event<Resource<List<Post>>>>(Event(Resource.Empty()))

    override val posts: StateFlow<Event<Resource<List<Post>>>>
        get() = _posts

    override fun getPosts(uid: String) {
        _posts.value = Event((Resource.Loading()))
        viewModelScope.launch {
            val result = repository.getPostsForProfile(uid)
            _posts.value = Event(result)
        }
    }

    private val _userFlow = MutableStateFlow<Event<Resource<User>>>(Event(Resource.Empty()))
    val userFlow: StateFlow<Event<Resource<User>>> = _userFlow

    fun loadProfile(uid: String) {
        _userFlow.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getUser(uid)
            _userFlow.value = Event((result))
        }
    }



    private val _toggleFollowState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Empty()))
    val toggleFollowState: StateFlow<Event<Resource<Boolean>>> = _toggleFollowState

    fun toggleFollow(uid: String) {
        val flow = flow {
            emit(repository.toggleFollow(uid))
        }
        _toggleFollowState.value = Event(Resource.Loading())
        viewModelScope.launch {
            flow.collect {
                _toggleFollowState.value = Event(it)
            }
        }
    }


    private val _followers = MutableStateFlow<Event<Resource<List<User>>>>(Event(Resource.Empty()))
    val followers: StateFlow<Event<Resource<List<User>>>> = _followers

    fun getFollowers(uid: String) {
        viewModelScope.launch {
            val result = repository.getFollowers(uid)
            _followers.value = Event((result))
        }
    }


    private val _following = MutableStateFlow<Event<Resource<List<User>>>>(Event(Resource.Empty()))
    val following: StateFlow<Event<Resource<List<User>>>> = _following

    fun getFollowing(uid: String) {
        viewModelScope.launch {
            val result = repository.getFollowing(uid)
            _followers.value = Event((result))
        }
    }


    private val _likes = MutableStateFlow<Event<Resource<List<User>>>>(Event(Resource.Empty()))
    val likes: StateFlow<Event<Resource<List<User>>>> = _likes

    fun getLikes(postId: String) {
        _likes.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getLikes(postId)
            _likes.value = Event(result)
        }
    }


}