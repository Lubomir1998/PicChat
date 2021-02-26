package com.example.picchat.data.requests

import java.util.*

data class Auth(
    val email: String,
    val password: String,
    val uid: String = UUID.randomUUID().toString()
)
