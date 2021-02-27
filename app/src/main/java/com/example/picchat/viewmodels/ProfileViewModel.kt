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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(private val repository: MainRepository): ViewModel() {


    private val _userFlow = MutableStateFlow<Event<Resource<User>>>(Event(Resource.Empty()))
    val userFlow: StateFlow<Event<Resource<User>>> = _userFlow

    fun loadProfile(uid: String) {
        _userFlow.value = Event(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getUser(uid)
            _userFlow.value = Event((result))
        }
    }

}