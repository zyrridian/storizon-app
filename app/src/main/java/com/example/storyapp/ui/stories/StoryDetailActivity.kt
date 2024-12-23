package com.example.storyapp.ui.stories

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.databinding.ActivityDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupStoryData()
        setupBackButton()

    }

    private fun setupStoryData() {
        intent.getParcelableExtra<StoryResponseItem>(EXTRA_STORY)?.let { story ->
            binding.apply {
                nameTextView.text = story.name
                descriptionTextView.text = story.description
                createAtTextView.text = formatDate(story.createdAt)
                Glide.with(this@StoryDetailActivity).load(story.photoUrl).into(imageView)
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    companion object {
        const val EXTRA_STORY = "EXTRA_STORY"
    }

}