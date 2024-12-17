package com.example.storyapp.ui.stories

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
import com.example.storyapp.data.LoadingStateAdapter
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.maps.MapsActivity
import com.example.storyapp.ui.settings.SettingsActivity
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.addstory.AddStoryActivity
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var drawerLayout: DrawerLayout
    private var token: String? = null
    private val viewModel: StoryViewModel by viewModels { ViewModelFactory.getInstance(this@StoryActivity) }

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

        lifecycleScope.launch {
            token = "Bearer ${SettingsPreferences.getInstance(dataStore).getTokenSession().first()}"
            token?.let { viewModel.setToken(it) }
        }

        drawerLayout = binding.drawerLayout

        updateDrawerHeader()

        setClickListeners()
        setUpNavigationDrawer()
//        setUpRecyclerView()
//fetch
        viewModel.story.observe(this) { result ->
            storyAdapter.submitData(lifecycle, result)
        }

        storyAdapter = StoryAdapter()//.apply { setLoadingState(true) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyAdapter.retry()
                }
            )
        }

        viewModel.story.observe(this) { result ->
            storyAdapter.submitData(lifecycle, result)
        }
    }

    override fun onResume() {
        super.onResume()
//        token?.let { fetchStories(it) }
    }

    private fun setClickListeners() {
        binding.menuButton.setOnClickListener { drawerLayout.open() }
        binding.mapsButton.setOnClickListener { startActivity(Intent(this, MapsActivity::class.java)) }
        binding.fab.setOnClickListener { startActivity(Intent(this, AddStoryActivity::class.java)) }
//        binding.swipeRefresh.setOnRefreshListener { token?.let { fetchStories(it) } }
    }

    private fun setUpNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this@StoryActivity, SettingsActivity::class.java))
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

//    private fun fetchStories(token: String) {
//        viewModel.story.observe(t)
//        binding.swipeRefresh.isRefreshing = false
//    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

}