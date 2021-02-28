package com.example.picchat.repositories

import android.content.SharedPreferences
import android.net.Uri
import com.example.picchat.data.ApiService
import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.Post
import com.example.picchat.data.requests.ToggleLikeRequest
import com.example.picchat.data.requests.UpdateUserRequest
import com.example.picchat.other.Constants.DEFAULT_PROFILE_IMG_URL
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.safeCall
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    suspend fun getPostsForProfile(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val posts = api.getPostsForProfile(uid).body()

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

                Resource.Success(it)
            } ?: Resource.Error("Error occurred")
        }
    }

    suspend fun searchUsers(query: String) = withContext(Dispatchers.IO) {
        safeCall {
            val users = api.searchUsers(query).body()

            users?.let {
                val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

                it.forEach { user ->
                    user.isFollowing = currentUid in user.followers
                }

                Resource.Success(it)
            } ?:  Resource.Error("Error occurred")
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

    private suspend fun updateProfilePicture(imgUri: Uri) = withContext(Dispatchers.IO) {
        val uid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
        val storageRef = storage.getReference(uid)
        val user = getUser(uid).data!!
        if (user.profileImgUrl != DEFAULT_PROFILE_IMG_URL) {
            storage.getReferenceFromUrl(user.profileImgUrl).delete().await()
        }
        storageRef.putFile(imgUri).await().metadata?.reference?.downloadUrl?.await()
    }


    suspend fun updateProfile(imgUri: Uri?, username: String, bio: String) = withContext(Dispatchers.IO) {
        safeCall {
            val imageUrl = imgUri?.let { uri ->
                updateProfilePicture(uri).toString()
            }
            val response = api.updateProfile(UpdateUserRequest(imageUrl, username, bio))
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Profile updated")
            }
            else {
                Resource.Error(response.body()?.message ?: response.message())
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

    suspend fun addComment(text: String, postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val authorUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

            val comment = Comment(
                    authorUid,
                    postId,
                    text
            )

            val response = api.addComment(comment)

            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error("Something went wrong")
            }
        }
    }

    suspend fun getComments(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val comments = api.getComments(postId).body()

            comments?.let {
                it.forEach { comment ->
                    val author = api.getUserById(comment.authorUid) ?: throw Exception()

                    comment.apply {
                        profileImfUrl = author.profileImgUrl
                        username = author.username
                    }

                }
                Resource.Success(it)
            } ?: Resource.Error("Something went wrong")

        }
    }

}