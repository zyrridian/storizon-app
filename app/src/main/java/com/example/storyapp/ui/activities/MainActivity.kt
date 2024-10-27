package com.example.storyapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory
import com.example.storyapp.ui.adapters.StoryAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private val viewModel: StoryViewModel by viewModels() {
        ViewModelFactory.getInstance(this@MainActivity)
    }

    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.token.observe(this) {
            if (it != null) {
                token = "Bearer $it"
                fetchStories(token!!)
            }
        }

        storyAdapter = StoryAdapter()
        storyAdapter.setLoadingState(true)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }

        binding.uploadImageView.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }

        binding.settingsImageView.setOnClickListener {
//            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
//            startActivity(intent)
            viewModel.logout()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            token?.let { fetchStories(it) }
        }

        observerStories()

    }

    override fun onResume() {
        super.onResume()
        token?.let { fetchStories(it) }
    }

    private fun observerStories() {
        viewModel.stories.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading()
                    storyAdapter.setLoadingState(true)
                }

                is Resource.Success -> {
                    hideLoading()
                    storyAdapter.setLoadingState(false)
                    storyAdapter.submitList(result.data) {
                        binding.recyclerView.smoothScrollToPosition(0)
                    }
                    binding.swipeRefresh.isRefreshing = false
                }

                is Resource.Error -> {
                    hideLoading()
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, "Failed to load stories", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchStories(token: String) {
        viewModel.fetchAllStories(token)
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showLoading() {
//        binding.swipeRefresh.isRefreshing = true
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
//        binding.swipeRefresh.isRefreshing = false
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
    }

}