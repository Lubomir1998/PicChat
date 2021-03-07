package com.example.picchat.viewmodels

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel
@Inject constructor(
        private val repository: MainRepository,
        private val sharedPrefs: SharedPreferences
): ViewModel() {

    private val _updateProfileState = MutableStateFlow<Event<Resource<String>>>(Event(Resource.Empty()))

    val updateProfileState: StateFlow<Event<Resource<String>>> = _updateProfileState

    fun updateProfile(imgUri: Uri?, username: String, bio: String) {
        if (username.isEmpty()) {
            _updateProfileState.value = Event(Resource.Error("Username can't be empty"))
            return
        }

        _updateProfileState.value = Event(Resource.Loading())

        viewModelScope.launch {
            val result = repository.updateProfile(imgUri, username, bio)
            _updateProfileState.value = Event(result)
        }

    }


    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("dark", false))

    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun changeTheme(isNightMode: Boolean) {
        _isDarkTheme.value = !isNightMode
    }


    private val _imgUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val imgUri: StateFlow<Uri?> = _imgUri

    fun setImgUri(uri: Uri) {
        _imgUri.value = uri
    }

}