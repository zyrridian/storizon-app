package com.example.storyapp.ui.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.databinding.ItemDataStoryBinding
import com.example.storyapp.databinding.ItemShimmerStoryBinding
import com.example.storyapp.ui.activities.DetailActivity

class StoryAdapter(
    private var isLoading: Boolean = true,
//    private val onFavoriteClick: (StoryEntity) -> Unit
) : ListAdapter<StoryEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SHIMMER) {
            val binding = ItemShimmerStoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ShimmerViewHolder(binding)
        } else {
            val binding = ItemDataStoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            DataViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val story = getItem(position)
            holder.bind(story)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else super.getItemCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadingState(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(binding: ItemShimmerStoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DataViewHolder(private var binding: ItemDataStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryEntity) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imageView)
            binding.titleTextView.text = story.name
            binding.descriptionTitle.text = story.description
            itemView.setOnClickListener {
                val intent = Intent(binding.root.context, DetailActivity::class.java)
                intent.putExtra("EXTRA_STORY", story)
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_SHIMMER = 0
        private const val VIEW_TYPE_DATA = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }

    }

}