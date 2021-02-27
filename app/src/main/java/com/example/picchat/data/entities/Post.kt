package com.example.picchat.data.entities

import com.google.gson.annotations.Expose
import java.util.*

data class Post(
    val imgUrl: String,
    val authorUid: String,
    val description: String,
    val date: Long,
    var likes: List<String> = listOf(),
    var comments: Int = 0,
    @Expose(serialize = false, deserialize = false)
    var authorUsername: String = "",
    @Expose(serialize = false, deserialize = false)
    var authorProfileImgUrl: String = "",
    @Expose(serialize = false, deserialize = false)
    var isLiked: Boolean = false,
    @Expose(serialize = false, deserialize = false)
    var isLiking: Boolean = false,
    val id: String = UUID.randomUUID().toString()
)
