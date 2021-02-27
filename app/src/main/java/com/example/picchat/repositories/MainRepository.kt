package com.example.picchat.repositories

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.picchat.data.ApiService
import com.example.picchat.data.entities.Post
import com.example.picchat.data.requests.ToggleLikeRequest
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.safeCall
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class MainRepository
@Inject constructor(private val api: ApiService) {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val storage = Firebase.storage

    suspend fun getPostsOfFollowing(): Resource<List<Post>> = withContext(Dispatchers.IO) {
        safeCall {
            val posts = api.getPostsOfFollowing().body()

            posts?.let {
                val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
                it.forEach { post ->
                    val postAuthor = api.getUserById(post.authorUid)!!
                    post.apply {
                        authorUsername = postAuthor.username
                        authorProfileImgUrl = postAuthor.profileImgUrl
                        isLiked = currentUid in post.likes
                    }
                }

                Resource.Success(posts)
            } ?: Resource.Error("Error occurred")
        }
    }

    suspend fun createPost(imageUri: Uri, text: String) = withContext(Dispatchers.IO) {
        safeCall {
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
            val postId = UUID.randomUUID().toString()

            val imageUploadResult = storage.getReference(postId).putFile(imageUri).await()
            val imgUrl = imageUploadResult.metadata?.reference?.downloadUrl?.await().toString()

            val post = Post(
                imgUrl,
                currentUid,
                text,
                System.currentTimeMillis(),
                id = postId
            )

            val response = api.createPost(post)

            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error(response.body()?.message ?: response.message(), null)
            }

        }
    }

    suspend fun toggleLike(postId: String, uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.toggleLike(ToggleLikeRequest(postId, uid))
            if(response.isSuccessful) {
                Resource.Success(Any())
            }
            else {
                Resource.Error("Something went wrong")
            }
        }
    }

    suspend fun getUser(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val user = api.getUserById(uid) ?: throw Exception()
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: throw Exception()
            val currentUser = api.getUserById(currentUid) ?: throw Exception()

            user.isFollowing = uid in currentUser.following

            Resource.Success(user)
        }
    }

}