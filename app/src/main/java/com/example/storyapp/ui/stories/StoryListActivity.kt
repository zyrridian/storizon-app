package com.example.storyapp.ui.stories

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.LoadingStateAdapter
import com.example.storyapp.databinding.ActivityStoryListBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.addstory.AddStoryActivity
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryListBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLat: Double? = null
    private var userLon: Double? = null
    private var token: String? = null
    private val viewModel: StoryViewModel by viewModels { ViewModelFactory.getInstance(this@StoryListActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeComponents()
        requestLocationPermission()
        setupRecyclerView()
        setupSwipeRefresh()
        observeStories()
        fetchTokenAndStories()

        binding.backButton.setOnClickListener { finish() }
        binding.fab.setOnClickListener { startActivity(Intent(this, AddStoryActivity::class.java)) }

    }

    private fun initializeComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storyAdapter = StoryAdapter()
    }

    private fun requestLocationPermission() {
        val locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getUserLocation()
                } else {
                    showToast(this, "Location permission denied")
                    storyAdapter.setUserLocation(null, null, false)
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
                storyAdapter.setUserLocation(userLat, userLon, true)
            } else {
                showToast(this, "Unable to get location")
                storyAdapter.setUserLocation(null, null, true)
            }
        }.addOnFailureListener {
            showToast(this, "Failed to get location")
            storyAdapter.setUserLocation(null, null, true)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoryListActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            token?.let {
                storyAdapter.setLoadingState(true) // Enable shimmer
                fetchStories(it)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getUserLocation()
                } else {
                    storyAdapter.setUserLocation(null, null, false)
                }
            }
        }
    }

    private fun observeStories() {
        viewModel.story.observe(this) { pagingData ->
            storyAdapter.setLoadingState(false)
            storyAdapter.submitData(lifecycle, pagingData)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun fetchTokenAndStories() {
        lifecycleScope.launch {
            storyAdapter.setLoadingState(true) // Enable shimmer
            token = "Bearer ${SettingsPreferences.getInstance(dataStore).getTokenSession().first()}"
            token?.let { fetchStories(it) }
        }
    }

    private fun fetchStories(token: String) {
        viewModel.fetchStories(token)
    }

}