package com.example.picchat.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.data.entities.User
import com.example.picchat.databinding.SearchUserItemBinding
import javax.inject.Inject

class SearchUserAdapter
@Inject constructor(private val glide: RequestManager): ListAdapter<User, SearchUserAdapter.SearchUserViewHolder>(SearchDiffCallBack()) {

    class SearchUserViewHolder(itemView: SearchUserItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val userImg = itemView.searchUserImg
        val usernameTv = itemView.searchUserNameTv
        val buttonFollow = itemView.btnSearchFollow
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val binding = SearchUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = getItem(position)

        holder.apply {
            glide.load(user.profileImgUrl).into(userImg)
            usernameTv.text = user.username

            buttonFollow.apply {
                text = if (user.isFollowing) {
                    "Following"
                } else {
                    "Follow"
                }
                setTextColor(if (user.isFollowing) Color.BLACK else Color.WHITE)
                setBackgroundColor(if (user.isFollowing) Color.WHITE else Color.parseColor("#14B6FA"))

            }
            itemView.setOnClickListener {
                onUserClicked?.let {
                    it(user)
                }
            }
        }

    }


    private var onUserClicked: ((User) -> Unit)? = null

    fun setOnUserClicked(listener: ((User) -> Unit)) {
        onUserClicked = listener
    }


    class SearchDiffCallBack: DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return  oldItem == newItem
        }
    }
}