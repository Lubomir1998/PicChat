package com.example.picchat.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.data.entities.Post
import com.example.picchat.databinding.PostItemBinding
import com.example.picchat.other.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PostAdapter
@Inject constructor(
        private val glide: RequestManager,
        private val sharedPreferences: SharedPreferences
): ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallBack()) {

    class PostViewHolder(itemView: PostItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val postAuthorImg = itemView.postAuthorImg
        val postAuthorUsernameTv = itemView.postAuthorUsername
        val postImg = itemView.postImg
        val btnLike = itemView.btnLikePost
        val btnComment = itemView.btnCommentPost
        val postLikesTv = itemView.postLikesTv
        val postDescriptionTv = itemView.postDescriptionTv
        val postCommentsTv = itemView.postCommentsTv
        val dateTv = itemView.postDateTv
        val deletePostImg = itemView.deleteImgBtn
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        val isDartTheme = sharedPreferences.getBoolean("dark", false)
        val currentUid = sharedPreferences.getString(Constants.KEY_UID, Constants.NO_UID) ?: Constants.NO_UID

        holder.apply {
            glide.load(post.authorProfileImgUrl).into(postAuthorImg)
            postAuthorUsernameTv.text = post.authorUsername
            glide.load(post.imgUrl).into(postImg)

            if(currentUid == post.authorUid) {
                deletePostImg.visibility = View.VISIBLE
            }
            else {
                deletePostImg.visibility = View.GONE
            }

            postLikesTv.text = when {
                post.likes.isEmpty() -> {
                    "No likes"
                }
                post.likes.size == 1 -> {
                    "1 like"
                }
                else -> {
                    "${post.likes.size} likes"
                }
            }

            postDescriptionTv.isVisible = post.description.isNotEmpty().also {
                if(it) {
                    val usernameBuilder = SpannableStringBuilder("${post.authorUsername}  ${post.description}")

                    val usernameColorSpan = ForegroundColorSpan(if (isDartTheme) Color.WHITE else Color.BLACK)
                    val usernameStyleSpan = StyleSpan(android.graphics.Typeface.BOLD)

                    usernameBuilder.apply {
                        setSpan(usernameColorSpan, 0, post.authorUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        setSpan(usernameStyleSpan, 0, post.authorUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    }


                    postDescriptionTv.text = usernameBuilder

                }
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = dateFormat.format(post.date)

            dateTv.text = date

            btnLike.setImageResource(if(post.isLiked) R.drawable.liked_img else R.drawable.like_img)

            postCommentsTv.isVisible = (post.comments > 0).also {
                if(it) postCommentsTv.text = when (post.comments) {
                    1 -> "1 comment"
                    else -> "${post.comments} comments"
                }
            }

            postCommentsTv.setOnClickListener {
                onCommentTvClickListener?.let {
                    it(post, position)
                }
            }


            postAuthorUsernameTv.setOnClickListener {
                onUsernameClickListener?.let {
                    it(post.authorUid, position)
                }
            }

            postLikesTv.setOnClickListener {
                onLikesClickListener?.let {
                    it(post)
                }
            }

            btnComment.setOnClickListener {
                onCommentTvClickListener?.let {
                    it(post, position)
                }
            }

            btnLike.setOnClickListener {
                if(!post.isLiking) onLikeBtnClickListener?.let { it(post, position) }
            }

            deletePostImg.setOnClickListener {
                onDeletePostClickListener?.let {
                    it(post)
                }
            }

        }

    }


    private var onCommentTvClickListener: ((Post, Int) -> Unit)? = null

    fun setOnCommentTvClickListener(listener: ((Post, Int) -> Unit)) {
        onCommentTvClickListener = listener
    }



    private var onUsernameClickListener: ((String, Int) -> Unit)? = null

    fun setOnUsernameClickListener(listener: (String, Int) -> Unit) {
        onUsernameClickListener = listener
    }



    private var onLikesClickListener: ((Post) -> Unit)? = null

    fun setOnLikesClickListener(listener: (Post) -> Unit) {
        onLikesClickListener = listener
    }


    private var onLikeBtnClickListener: ((Post, Int) -> Unit)? = null

    fun setOnLikeBtnClickListener(listener: (Post, Int) -> Unit) {
        onLikeBtnClickListener = listener
    }


    private var onDeletePostClickListener: ((Post) -> Unit)? = null

    fun setOnDeletePostClickListener(listener: (Post) -> Unit) {
        onDeletePostClickListener = listener
    }




    class PostDiffCallBack: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}