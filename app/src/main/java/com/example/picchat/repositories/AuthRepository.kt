package com.example.picchat.repositories

import com.example.picchat.data.ApiService
import com.example.picchat.data.requests.AccountRequest
import com.example.picchat.data.requests.Auth
import com.example.picchat.other.Resource
import com.example.picchat.other.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository
@Inject constructor(private val api: ApiService) {

    suspend fun register(username: String, email: String, password: String) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.register(Auth(email, password), username)
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
               Resource.Error(response.body()?.message ?: response.message())
            }
        }
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.login(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error(response.body()?.message ?: response.message())
            }
        }
    }

    suspend fun getUid(): String? = api.getUid()

}