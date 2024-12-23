package com.example.storyapp.ui.stories

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.databinding.ItemDataStoryBinding
import com.example.storyapp.databinding.ItemShimmerStoryBinding
import com.example.storyapp.utils.calculateDistance
import com.example.storyapp.utils.formatDistanceText
import com.example.storyapp.utils.formatTimeAgo


class StoryHomeAdapter(
    private var isLoading: Boolean = true,
//    private val onFavoriteClick: (StoryEntity) -> Unit
) : ListAdapter<StoryResponseItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var userLat: Double? = null
    private var userLon: Double? = null
    private var isPermissionGranted: Boolean = false

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
            return DataViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val story = getItem(position)
            if (story != null) {
                holder.bind(story, userLat, userLon, isPermissionGranted)
            }
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

    fun setUserLocation(lat: Double?, lon: Double?, permissionGranted: Boolean) {
        userLat = lat
        userLon = lon
        isPermissionGranted = permissionGranted
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(binding: ItemShimmerStoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DataViewHolder(private var binding: ItemDataStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryResponseItem, userLat: Double?, userLon: Double?, isPermissionGranted: Boolean) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imageView)
            binding.authorTextView.text = "by ${story.name}"
            binding.titleTextView.text = story.description
            binding.timeTextView.text = formatTimeAgo(story.createdAt)

            val locationText = when {
                isPermissionGranted && story.lat != null && story.lon != null && userLat != null && userLon != null -> {
                    val distance = calculateDistance(userLat, userLon, story.lat, story.lon)
                    formatDistanceText(distance) // e.g., "2.4 km" or "< 200 m"
                }
                isPermissionGranted -> itemView.context.getString(R.string.no_location)
                story.lat != null && story.lon != null -> itemView.context.getString(R.string.enable_location)
                else -> itemView.context.getString(R.string.no_location)
            }

            binding.locationTextView.text = locationText

            itemView.setOnClickListener {
                val intent = Intent(binding.root.context, StoryDetailActivity::class.java)
                intent.putExtra("EXTRA_STORY", story)
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_SHIMMER = 0
        private const val VIEW_TYPE_DATA = 1

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