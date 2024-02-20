package com.example.harmonycare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.harmonycare.data.Comment
import com.example.harmonycare.data.Post
import com.example.harmonycare.databinding.CommentItemBinding
import com.example.harmonycare.databinding.CommunityItemBinding

class PostAdapter(private val dataList: List<Post>, private val isMyPost: Boolean, private val onItemClick: (Post) -> Unit, private val onDeleteClick: (Post) -> Unit) : RecyclerView.Adapter<PostAdapter.PostViewHolder>()  {
    inner class PostViewHolder(private val binding: CommunityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = dataList[position]
                    onItemClick(post)
                }
            }
        }
        fun bind(post: Post) {
            binding.textTitle.text = post.title
            binding.textCaption.text = post.content
            if (isMyPost) {
                binding.buttonDelete.visibility = View.VISIBLE
                binding.buttonDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val post = dataList[position]
                        onDeleteClick(post)
                    }
                }
            } else {
                binding.buttonDelete.visibility = View.GONE
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
        val binding = CommunityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostAdapter.PostViewHolder, position: Int) {
        val post = dataList[position]
        holder.bind(post)
        holder.itemView.setOnClickListener {
            onItemClick(post)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

class CommentAdapter(private val dataList: List<Comment>, private val onDeleteClick: (Comment) -> Unit): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    inner class CommentViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.textComment.text = comment.content
            if (comment.isMyComment == true) {
                binding.buttonDelete.visibility = View.VISIBLE
                binding.buttonDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val checklist = dataList[position]
                        onDeleteClick(checklist)
                    }
                }
            } else {
                binding.buttonDelete.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val post = dataList[position]
        holder.bind(post)
    }
}