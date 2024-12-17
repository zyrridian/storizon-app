package com.example.storyapp.ui.stories

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.databinding.ItemDataStoryBinding

class StoryAdapter(
//    private var isLoading: Boolean = true,
//    private val onFavoriteClick: (StoryEntity) -> Unit
) : PagingDataAdapter<StoryResponseItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_DATA
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == VIEW_TYPE_SHIMMER) {
//            val binding = ItemShimmerStoryBinding.inflate(
//                LayoutInflater.from(parent.context), parent, false
//            )
//            ShimmerViewHolder(binding)
//        } else {
        val binding = ItemDataStoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataViewHolder(binding)
//        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {// && !isLoading) {
            val story = getItem(position)
            if (story != null) {
                holder.bind(story)
            }
        }
    }

//    override fun getItemCount(): Int {
//        return if (isLoading) 10 else super.getItemCount()
//    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun setLoadingState(isLoading: Boolean) {
//        this.isLoading = isLoading
//        notifyDataSetChanged()
//    }

//    class ShimmerViewHolder(binding: ItemShimmerStoryBinding) :
//        RecyclerView.ViewHolder(binding.root)

    class DataViewHolder(private var binding: ItemDataStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryResponseItem) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imageView)
            binding.titleTextView.text = story.name
            binding.descriptionTitle.text = story.description
            itemView.setOnClickListener {
                val intent = Intent(binding.root.context, StoryDetailActivity::class.java)
                intent.putExtra("EXTRA_STORY", story)
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object {

//        private const val VIEW_TYPE_SHIMMER = 0
//        private const val VIEW_TYPE_DATA = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryResponseItem>() {
            override fun areItemsTheSame(
                oldItem: StoryResponseItem,
                newItem: StoryResponseItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: StoryResponseItem,
                newItem: StoryResponseItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }

    }

}