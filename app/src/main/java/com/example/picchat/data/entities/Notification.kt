package com.example.picchat.data.entities

import com.google.gson.annotations.Expose
import java.util.*

data class Notification (
        val senderUid: String,
        val recipientUid: String,
        val message: String,
        val postId: String? = null,
        val postImgUrl: String? = null,
        @Expose(serialize = false, deserialize = false)
        var isFollowing: Boolean = false,
        val timestamp: Long = System.currentTimeMillis(),
        @Expose(serialize = false, deserialize = false)
        var senderUsername: String = "",
        @Expose(serialize = false, deserialize = false)
        var senderProfileImgUrl: String = "",
        val id: String = UUID.randomUUID().toString()
)
