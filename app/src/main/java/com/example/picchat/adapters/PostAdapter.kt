package com.example.picchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.R
import com.example.picchat.data.entities.Post
import com.example.picchat.databinding.PostItemBinding
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PostAdapter
@Inject constructor(private val glide: RequestManager): ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallBack()) {

    class PostViewHolder(itemView: PostItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val postAuthorImg = itemView.postAuthorImg
        val postAuthorUsernameTv = itemView.postAuthorUsername
        val postImg = itemView.postImg
        val btnLike = itemView.btnLikePost
        val btnComment = itemView.btnCommentPost
        val postLikesTv = itemView.postLikesTv
        val usernameDescriptionTv = itemView.postUsernameDescriptionTv
        val postDescriptionTv = itemView.postDescriptionTv
        val postCommentsTv = itemView.postCommentsTv
        val dateTv = itemView.postDateTv
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        holder.apply {
            glide.load(post.authorProfileImgUrl).into(postAuthorImg)
            postAuthorUsernameTv.text = post.authorUsername
            glide.load(post.imgUrl).into(postImg)

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

            usernameDescriptionTv.isVisible = post.description.isNotEmpty()
            postDescriptionTv.isVisible = post.description.isNotEmpty().also {
                if(it) {
                    usernameDescriptionTv.text = post.authorUsername
                    postDescriptionTv.text = post.description
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

            usernameDescriptionTv.setOnClickListener {
                onUsernameClickListener?.let {
                    it(post.authorUid, position)
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




    class PostDiffCallBack: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}