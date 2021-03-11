package com.example.picchat.data

import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.Post
import com.example.picchat.data.entities.User
import com.example.picchat.data.requests.*
import com.example.picchat.data.responses.SimpleResponse
import com.example.picchat.other.Constants.CONTENT_TYPE
import com.example.picchat.other.Constants.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/register/{username}")
    suspend fun register(@Body auth: Auth, @Path("username") username: String): Response<SimpleResponse>

    @POST("/login/{token}")
    suspend fun login(@Body accountRequest: AccountRequest, @Path("token") token: String): Response<SimpleResponse>

    @POST("/removeToken")
    suspend fun removeToken(@Body removeTokenRequest: RemoveTokenRequest): Response<SimpleResponse>

    @GET("/getTokens/{uid}")
    suspend fun getTokens(@Path("uid") id: String) : Response<List<String>>

    @GET("/getUserByEmail/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): User?

    @GET("/getUid")
    suspend fun getUid(): String?

    @GET("/postOfFollowing")
    suspend fun getPostsOfFollowing(): Response<List<Post>>

    @GET("getUser/{id}")
    suspend fun getUserById(@Path("id") id: String): User?

    @GET("getPostsForUser/{uid}")
    suspend fun getPostsForProfile(@Path("uid") id: String): Response<List<Post>>

    @POST("/createPost")
    suspend fun createPost(@Body post: Post): Response<SimpleResponse>

    @POST("/addComment")
    suspend fun addComment(@Body comment: Comment): Response<SimpleResponse>

    @POST("/addNotification")
    suspend fun addNotification(@Body notification: Notification): Response<SimpleResponse>

    @GET("/getActivity/{uid}")
    suspend fun getActivity(@Path("uid") uid: String): Response<List<Notification>>

    @POST("/toggleLike")
    suspend fun toggleLike(@Body toggleLikeRequest: ToggleLikeRequest): Response<ResponseBody>

    @GET("/searchUsers/{query}")
    suspend fun searchUsers(@Path("query") query: String): Response<List<User>>

    @POST("/updateUser")
    suspend fun updateProfile(@Body updateUserRequest: UpdateUserRequest): Response<SimpleResponse>

    @GET("/getComments/{id}")
    suspend fun getComments(@Path("id") postId: String): Response<List<Comment>>

    @GET("/getPost/{id}")
    suspend fun getPostById(@Path("id") postId: String): Response<Post?>

    @POST("/toggleFollow")
    suspend fun toggleFollow(@Body toggleFollowRequest: ToggleFollowRequest): Response<ResponseBody>

    @GET("/getFollowers/{uid}")
    suspend fun getFollowers(@Path("uid") uid: String): Response<List<String>>

    @GET("/getFollowing/{uid}")
    suspend fun getFollowing(@Path("uid") uid: String): Response<List<String>>

    @POST("/deleteComment")
    suspend fun deleteComment(@Body comment: Comment): Response<SimpleResponse>

    @POST("/deletePost")
    suspend fun deletePost(@Body post: Post): Response<SimpleResponse>

}