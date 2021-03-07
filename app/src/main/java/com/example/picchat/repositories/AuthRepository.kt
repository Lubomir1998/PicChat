package com.example.picchat.repositories

import android.content.SharedPreferences
import com.example.picchat.data.ApiService
import com.example.picchat.data.requests.AccountRequest
import com.example.picchat.data.requests.Auth
import com.example.picchat.other.Constants.KEY_USERNAME
import com.example.picchat.other.Resource
import com.example.picchat.other.safeCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository
@Inject constructor(private val api: ApiService) {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

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
                val user = api.getUserByEmail(email)
                user?.let {
                    sharedPrefs.edit().putString(KEY_USERNAME, it.username).apply()
                }
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error(response.body()?.message ?: response.message())
            }
        }
    }

    suspend fun getUid(): String? = api.getUid()
    
}