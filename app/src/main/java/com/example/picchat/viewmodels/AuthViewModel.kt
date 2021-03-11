package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(private val repository: AuthRepository): ViewModel() {

    private val _registerState = MutableStateFlow<Event<Resource<String?>>>(Event(Resource.Empty()))

    val registerState: StateFlow<Event<Resource<String?>>> = _registerState

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registerState.value = Event(Resource.Error("Empty fields"))
            return
        }
        if (password != confirmPassword) {
            _registerState.value = Event(Resource.Error("Passwords do not match"))
            return
        }

        _registerState.value = Event(Resource.Loading())

        viewModelScope.launch {
            val result = repository.register(username, email, password)
            _registerState.value = Event(result)
        }

    }


    private val _loginState = MutableStateFlow<Event<Resource<String?>>>(Event(Resource.Empty()))

    val loginState: StateFlow<Event<Resource<String?>>> = _loginState

    fun login(email: String, password: String, token: String) {
        if(email.isEmpty() || password.isEmpty()) {
            _loginState.value = Event(Resource.Error("Empty fields"))
            return
        }

        _registerState.value = Event(Resource.Loading())

        viewModelScope.launch {
            val result = repository.login(email, password, token)
            _loginState.value = Event(result)
        }

    }


    private val _uid = MutableStateFlow<String?>(NO_UID)

    val uid: StateFlow<String?> = _uid

    private val flow = flow {
        emit(repository.getUid())
    }

   fun getUid() {
       viewModelScope.launch {
           flow
               .collect {
                   _uid.value = it
               }
       }
   }


}