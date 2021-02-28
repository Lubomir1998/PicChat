package com.example.picchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.data.entities.Comment
import com.example.picchat.databinding.CommentItemBinding
import javax.inject.Inject

class CommentAdapter
@Inject constructor(private val glide: RequestManager): ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    class CommentViewHolder(itemView: CommentItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val profileImg = itemView.commentAuthorImg
        val authorUsername = itemView.commentAuthorUsername
        val commentText = itemView.commentTextTv
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)

        holder.apply {
            glide.load(comment.profileImfUrl).into(profileImg)
            authorUsername.text = comment.username
            commentText.text = comment.text

            authorUsername.setOnClickListener {
                onUsernameClickListener?.let {
                    it(comment.authorUid)
                }
            }

            profileImg.setOnClickListener {
                onUsernameClickListener?.let {
                    it(comment.authorUid)
                }
            }
        }
    }



    private var onUsernameClickListener: ((String) -> Unit)? = null

    fun setOnUsernameClickListener(listener: (String) -> Unit) {
        onUsernameClickListener = listener
    }




    class CommentDiffCallback: DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}