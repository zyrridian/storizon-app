package com.example.storyapp.ui.stories

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.maps.MapsActivity
import com.example.storyapp.ui.settings.SettingsActivity
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.addstory.AddStoryActivity
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.Resource
import com.example.storyapp.utils.StreakManager
import com.example.storyapp.utils.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: StoryViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var token: String? = null
    private var homeAdapter = StoryHomeAdapter()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLat: Double? = null
    private var userLon: Double? = null

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

        // Set daily streak
        val streakCount = StreakManager.updateLoginStreak(this)
        binding.streakTextView.text = streakCount.toString()

        // set welcome name
        val settingsPreferences = SettingsPreferences.getInstance(dataStore)
        lifecycleScope.launch {
            settingsPreferences.getUserName().collect { name ->
                binding.welcome.text = getString(R.string.hello, name) ?: getString(R.string.guest)
            }
        }

        // set map card
        Glide.with(this).load(R.drawable.img_maps).into(binding.imageView)

        // Request location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()

        // initial shimmer on
        homeAdapter.setLoadingState(true)

        setClickListeners()
        setupRecyclerView()
        setupSwipeRefresh()
        observeStories()
        fetchTokenAndStories()
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences("StoryAppPrefs", MODE_PRIVATE)
        val totalWordCount = sharedPreferences.getInt("total_word_count", 0)

        // Update the TextView with the total word count
        binding.wordTextView.text = totalWordCount.toString()
    }

    private fun requestLocationPermission() {
        val locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getUserLocation()
                } else {
                    showToast(this, "Location permission denied")
                    // Update adapter to indicate permission is denied
                    homeAdapter.setUserLocation(null, null, false)
                }
            }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLat = location.latitude
                userLon = location.longitude
                // Pass the location and permission status to the adapter
                homeAdapter.setUserLocation(userLat, userLon, true)
            } else {
                showToast(this, "Unable to get location")
                // Pass null location but indicate permission is granted
                homeAdapter.setUserLocation(null, null, true)
            }
        }.addOnFailureListener {
            showToast(this, "Failed to get location")
            // Pass null location but indicate permission is granted
            homeAdapter.setUserLocation(null, null, true)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = homeAdapter // Set the adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            token?.let {
                fetchStories(it)
                // Re-check location permission and update adapter
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getUserLocation()
                } else {
                    homeAdapter.setUserLocation(null, null, false)
                }
            }
        }
    }

    private fun observeStories() {
        viewModel.homeStories.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    val stories = resource.data
                    // Update RecyclerView adapter with stories
                    stories?.let {
                        // Assuming you have an adapter set up
                        homeAdapter.submitList(it)
                        binding.recyclerView.adapter = homeAdapter
                    }
                }

                is Resource.Error -> {
                    hideLoading()
                    showToast(this, resource.error ?: "An error occurred")
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun fetchTokenAndStories() {
        lifecycleScope.launch {
            token = "Bearer ${SettingsPreferences.getInstance(dataStore).getTokenSession().first()}"
            token?.let { fetchStories(it) }
        }
    }

    private fun fetchStories(token: String) {
        val size = 3 // Define the size of stories to fetch
        viewModel.fetchHomeStories(token, size)
    }

    private fun setClickListeners() {
        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.storyViewAll.setOnClickListener {
            val intent = Intent(this, StoryListActivity::class.java)
            startActivity(intent)
        }
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
        binding.mapsCard.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun showLoading() {
        homeAdapter.setLoadingState(true)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        homeAdapter.setLoadingState(false)
        binding.progressBar.visibility = View.GONE
    }

}