package com.example.picchat.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.picchat.data.entities.Notification
import com.example.picchat.databinding.NotificationItemPostBinding
import com.example.picchat.databinding.NotificationItemUserBinding
import javax.inject.Inject

class NotificationsAdapter
@Inject constructor(private val glide: RequestManager): ListAdapter<Notification, RecyclerView.ViewHolder>(NotificationDiffCallback()) {


    private val USER = 0
    private val POST = 1


    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        if(item.postId != null) return POST

        return USER
    }

    class NotificationPostViewHolder(itemView: NotificationItemPostBinding): RecyclerView.ViewHolder(itemView.root) {
        val profileImg = itemView.authorProfileImg
        val notificationMessage = itemView.messageTv
        val postImg = itemView.notificationPostImg
    }

    class NotificationUserViewHolder(itemView: NotificationItemUserBinding): RecyclerView.ViewHolder(itemView.root) {
        val profileImg = itemView.authorProfileImg
        val notificationMessage = itemView.messageTv
        val btnFollow = itemView.notificationsBtnFollow
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == POST) {
            val binding = NotificationItemPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NotificationPostViewHolder(binding)
        }
        else {
            val binding = NotificationItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NotificationUserViewHolder(binding)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)


        when(holder) {
            is NotificationPostViewHolder -> {
                holder.apply {
                    glide.load(item.senderProfileImgUrl).into(profileImg)

                    val usernameBuilder = SpannableStringBuilder("${item.senderUsername}  ${item.message}")

                    val usernameColorSpan = ForegroundColorSpan(Color.BLACK)
                    val usernameStyleSpan = StyleSpan(Typeface.BOLD)

                    usernameBuilder.apply {
                        setSpan(usernameColorSpan, 0, item.senderUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        setSpan(usernameStyleSpan, 0, item.senderUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    }


                    notificationMessage.text = usernameBuilder

                    glide.load(item.postImgUrl).into(postImg)

                    profileImg.setOnClickListener {
                        onProfileImgClickListener?.let {
                            it(item.senderUid)
                        }
                    }

                    postImg.setOnClickListener {
                        onPostClickListener?.let {
                            it(item.postId!!)
                        }
                    }

                }
            }


            is NotificationUserViewHolder -> {
                holder.apply {
                    glide.load(item.senderProfileImgUrl).into(profileImg)

                    val usernameBuilder = SpannableStringBuilder("${item.senderUsername}  ${item.message}")

                    val usernameColorSpan = ForegroundColorSpan(Color.BLACK)
                    val usernameStyleSpan = StyleSpan(Typeface.BOLD)

                    usernameBuilder.apply {
                        setSpan(usernameColorSpan, 0, item.senderUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        setSpan(usernameStyleSpan, 0, item.senderUsername.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    }


                    notificationMessage.text = usernameBuilder

                    btnFollow.apply {
                        setTextColor(if (item.isFollowing) Color.BLACK else Color.WHITE)
                        setBackgroundColor(if (item.isFollowing) Color.WHITE else Color.parseColor("#14B6FA"))
                        text = if (item.isFollowing) {
                            "Following"
                        }
                        else {
                            "Follow"
                        }

                        setOnClickListener {
                            onBtnFollowClickListener?.let {
                                it(item.senderUid, position)
                            }
                        }
                    }

                    profileImg.setOnClickListener {
                        onProfileImgClickListener?.let {
                            it(item.senderUid)
                        }
                    }

                }
            }
        }
    }




    private var onProfileImgClickListener: ((String) -> Unit)? = null

    fun setOnProfileImgClickListener(listener: (String) -> Unit) {
        onProfileImgClickListener = listener
    }


    private var onPostClickListener: ((String) -> Unit)? = null

    fun setOnPostClickListener(listener: (String) -> Unit) {
        onPostClickListener = listener
    }

    private var onBtnFollowClickListener: ((String, Int) -> Unit)? = null

    fun setOnBtnFollowClickListener(listener: (String, Int) -> Unit) {
        onBtnFollowClickListener = listener
    }



    class NotificationDiffCallback: DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}