package com.example.picchat.viewmodels

import androidx.lifecycle.ViewModel
import com.example.picchat.data.entities.Post
import com.example.picchat.other.Event
import com.example.picchat.other.Resource
import com.example.picchat.repositories.MainRepository
import kotlinx.coroutines.flow.StateFlow

abstract class BasePostViewModel(private val repository: MainRepository): ViewModel() {

    abstract val posts: StateFlow<Event<Resource<List<Post>>>>

    abstract fun getPosts(uid: String = "")

}