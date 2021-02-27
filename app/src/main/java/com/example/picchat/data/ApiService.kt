package com.example.picchat.data

import com.example.picchat.data.entities.Post
import com.example.picchat.data.entities.User
import com.example.picchat.data.requests.AccountRequest
import com.example.picchat.data.requests.Auth
import com.example.picchat.data.requests.ToggleLikeRequest
import com.example.picchat.data.responses.SimpleResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/register/{username}")
    suspend fun register(@Body auth: Auth, @Path("username") username: String): Response<SimpleResponse>

    @POST("login")
    suspend fun login(@Body accountRequest: AccountRequest): Response<SimpleResponse>

    @GET("/getUid")
    suspend fun getUid(): String?

    @GET("/postOfFollowing")
    suspend fun getPostsOfFollowing(): Response<List<Post>>

    @GET("getUser/{id}")
    suspend fun getUserById(@Path("id") id: String): User?

    @POST("/createPost")
    suspend fun createPost(@Body post: Post): Response<SimpleResponse>

    @POST("/toggleLike")
    suspend fun toggleLike(@Body toggleLikeRequest: ToggleLikeRequest): Response<ResponseBody>


}