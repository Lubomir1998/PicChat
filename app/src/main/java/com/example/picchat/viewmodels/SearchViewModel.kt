package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SearchViewModel
@Inject constructor(private val repository: MainRepository): ViewModel() {

    private val _users = MutableStateFlow<Event<Resource<List<User>>>>(Event((Resource.Empty())))

    val users: StateFlow<Event<Resource<List<User>>>> = _users

    fun searchUsers(query: String) {
        if(query.isEmpty()) return

        _users.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.searchUsers(query)
            _users.value = Event(result)
        }
    }


    private val _toggleFollowState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Empty()))
    val toggleFollowState: StateFlow<Event<Resource<Boolean>>> = _toggleFollowState


    fun toggleFollow(uid: String) {
        val flow = flow {
            emit(repository.toggleFollow(uid))
        }

        viewModelScope.launch {
            flow.collect {
                _toggleFollowState.value = Event(it)
            }
        }
    }


}