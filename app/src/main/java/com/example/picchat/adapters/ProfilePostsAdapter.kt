package com.example.picchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.data.entities.Post
import com.example.picchat.databinding.ProfilePostsItemBinding
import javax.inject.Inject

class ProfilePostsAdapter
@Inject constructor(private val glide: RequestManager): ListAdapter<Post, ProfilePostsAdapter.ProfilePostsViewHolder>(ProfilePostDiffCallBack()) {

    class ProfilePostsViewHolder(itemView: ProfilePostsItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val imageView = itemView.postImageview
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val binding = ProfilePostsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfilePostsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val post = getItem(position)

        glide.load(post.imgUrl).into(holder.imageView)

    }



    class ProfilePostDiffCallBack: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}