package com.example.picchat.repositories

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.picchat.data.ApiService
import com.example.picchat.data.FirebaseApi
import com.example.picchat.data.PushNotification
import com.example.picchat.data.entities.Comment
import com.example.picchat.data.entities.Notification
import com.example.picchat.data.entities.Post
import com.example.picchat.data.entities.User
import com.example.picchat.data.requests.RemoveTokenRequest
import com.example.picchat.data.requests.ToggleFollowRequest
import com.example.picchat.data.requests.ToggleLikeRequest
import com.example.picchat.data.requests.UpdateUserRequest
import com.example.picchat.other.Constants
import com.example.picchat.other.Constants.DEFAULT_PROFILE_IMG_URL
import com.example.picchat.other.Constants.KEY_UID
import com.example.picchat.other.Constants.NO_UID
import com.example.picchat.other.Resource
import com.example.picchat.other.safeCall
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class MainRepository
@Inject constructor(
    private val api: ApiService,
    private val firebaseApi: FirebaseApi
    ) {

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

                Resource.Success(it)
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

    suspend fun toggleFollow(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
            val response = api.toggleFollow(ToggleFollowRequest(uid))
            if(response.isSuccessful) {
                val user = getUser(uid).data ?: throw Exception()
                val isFollowed = currentUid in user.followers
                Resource.Success(isFollowed)
            }
            else {
                Resource.Error("Something went wrong")
            }
        }
    }

    suspend fun getPostById(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.getPostById(postId)

            if(response.isSuccessful) {
                val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID

                val post = response.body()
                post?.let {
                    val postAuthor = getUser(it.authorUid).data ?: throw Exception()

                    it.apply {
                        authorUsername = postAuthor.username
                        authorProfileImgUrl = postAuthor.profileImgUrl
                        isLiked = currentUid in likes
                    }

                    Resource.Success(listOf(it))
                } ?: Resource.Error("Something went wrong")
            }
            else {
                Resource.Error("Something went wrong")
            }
        }
    }

    private suspend fun getPost(postId: String): Post? {
        val response = api.getPostById(postId)

        return response.body()
    }

    suspend fun toggleLike(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
            val response = api.toggleLike(ToggleLikeRequest(postId, currentUid))
            if(response.isSuccessful) {
                val post = getPost(postId) ?: throw Exception()
                val isLiked = currentUid in post.likes
                Resource.Success(isLiked)
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

    suspend fun getFollowers(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val followersUids = api.getFollowers(uid).body() ?: throw Exception()
            val followers: MutableList<User> = mutableListOf()
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: throw Exception()

            for(id in followersUids) {
                val user = getUser(id).data!!
                user.isFollowing = currentUid in user.followers
                followers.add(user)
            }

            Resource.Success(followers.toList())
        }
    }

    suspend fun getFollowing(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val followingUids = api.getFollowing(uid).body() ?: throw Exception()
            val following: MutableList<User> = mutableListOf()
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: throw Exception()

            for(id in followingUids) {
                val user = getUser(id).data!!
                user.isFollowing = currentUid in user.followers
                following.add(user)
            }

            Resource.Success(following.toList())
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

    suspend fun getLikes(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val post = api.getPostById(postId).body() ?: throw Exception()
            val likes = post.likes

            val likesList = mutableListOf<User>()

            for (uid in likes) {
                val user = getUser(uid).data ?: throw Exception()
                likesList.add(user)
            }

            Resource.Success(likesList.toList())
        }
    }


    suspend fun addNotification(notification: Notification) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.addNotification(notification)

            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error("Something went wrong")
            }
        }
    }

    suspend fun deleteComment(comment: Comment) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.deleteComment(comment)
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error("Error")
            }
        }
    }

    suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.deletePost(post)
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                storage.getReferenceFromUrl(post.imgUrl).delete().await()
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error("Error")
            }
        }
    }

    suspend fun getActivity() = withContext(Dispatchers.IO) {
        safeCall {
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: throw Exception()
            val notifications = api.getActivity(currentUid).body() ?: throw Exception()

            notifications.onEach { notification ->
                val user = api.getUserById(notification.senderUid) ?: throw Exception()

                notification.apply {
                    senderUsername = user.username
                    senderProfileImgUrl = user.profileImgUrl
                    isFollowing = currentUid in user.followers
                }
            }

            Resource.Success(notifications)

        }
    }

    suspend fun removeTokenForUser() = withContext(Dispatchers.IO) {
        safeCall {
            val currentUid = sharedPrefs.getString(KEY_UID, NO_UID) ?: NO_UID
            val token = sharedPrefs.getString(Constants.KEY_TOKEN, "empty") ?: "empty"
            val response = api.removeToken(RemoveTokenRequest(currentUid, token))
            if(response.isSuccessful && currentUid != NO_UID) {
                Resource.Success(response.body()?.message)
            }
            else {
                Resource.Error("")
            }
        }
    }

    suspend fun getTokens(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.getTokens(uid)
            if(response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            }
            else {
                Resource.Empty(null)
            }
        }
    }

    //Firebase
    suspend fun sendPushNotification(pushNotification: PushNotification) {
        try {
            firebaseApi.postNotification(pushNotification)
        } catch (e: Exception) { }
    }



}