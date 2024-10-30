package com.example.storyapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory
import com.example.storyapp.ui.adapters.StoryAdapter
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.showToast
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var drawerLayout: DrawerLayout
    private val viewModel: StoryViewModel by viewModels { ViewModelFactory.getInstance(this@MainActivity) }
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

        drawerLayout = binding.drawerLayout

        setupViewModelObservers()
        setUpRecyclerView()
        setClickListeners()
        setUpNavigationDrawer()
        observerStories()

    }

    override fun onResume() {
        super.onResume()
        token?.let { fetchStories(it) }
    }

    private fun setupViewModelObservers() {
        viewModel.token.observe(this) { newToken ->
            newToken?.let {
                token = "Bearer $it"
                fetchStories(token!!)
            }
        }
        updateDrawerHeader()
    }

    private fun setUpRecyclerView() {
        storyAdapter = StoryAdapter().apply { setLoadingState(true) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }
    }

    private fun setClickListeners() {
        binding.menuButton.setOnClickListener { drawerLayout.open() }
        binding.fab.setOnClickListener { startActivity(Intent(this, AddStoryActivity::class.java)) }
        binding.swipeRefresh.setOnRefreshListener { token?.let { fetchStories(it) } }
    }

    private fun setUpNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    drawerLayout.close()
                    true
                }

                R.id.nav_about -> {
                    showToast(this, getString(R.string.toast_nav_about))
                    drawerLayout.close()
                    true
                }

                else -> false
            }
        }
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
                    showToast(this, getString(R.string.toast_failed_to_load_stories))
                }
            }
        }
    }


    private fun updateDrawerHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val textViewName = headerView.findViewById<TextView>(R.id.nav_header_title)
        val textViewEmail = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        val settingsPreferences = SettingsPreferences.getInstance(dataStore)

        lifecycleScope.launch {
            settingsPreferences.getUserName().collect { name ->
                textViewName.text = name ?: getString(R.string.guest)
            }
        }

        lifecycleScope.launch {
            settingsPreferences.getUserEmail().collect { email ->
                textViewEmail.text = email ?: getString(R.string.no_email_provided)
            }
        }

    }

    private fun fetchStories(token: String) {
        viewModel.fetchAllStories(token)
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

}