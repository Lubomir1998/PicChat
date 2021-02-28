package com.example.picchat.data.requests

data class UpdateUserRequest(
        val profileImgUrl: String?,
        val username: String,
        val bio: String
)
