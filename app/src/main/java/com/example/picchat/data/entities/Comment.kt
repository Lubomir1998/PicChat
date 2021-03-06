package com.example.picchat.data.entities

import com.google.gson.annotations.Expose
import java.util.*

data class Comment(
        val authorUid: String,
        val postId: String,
        val text: String,
        @Expose(serialize = false, deserialize = false)
        var profileImfUrl: String = "",
        @Expose(serialize = false, deserialize = false)
        var username: String = "",
        val id: String = UUID.randomUUID().toString()
)
