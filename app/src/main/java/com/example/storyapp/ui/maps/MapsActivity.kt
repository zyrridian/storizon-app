package com.example.storyapp.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.Resource
import com.example.storyapp.utils.showToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private var token: String? = null
    private val boundsBuilder = LatLngBounds.Builder()
    private val viewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this@MapsActivity)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.maps)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            token = "Bearer ${SettingsPreferences.getInstance(dataStore).getTokenSession().first()}"
            token?.let { viewModel.fetchStoriesWithLocation(it) }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        token?.let { viewModel.fetchStoriesWithLocation(it) }
    }

    override fun onResume() {
        super.onResume()
        token?.let { viewModel.fetchStoriesWithLocation(it) }
    }

    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)

        val dicodingSpace = LatLng(-6.8957643, 107.6338462)
        googleMap.addMarker(
            MarkerOptions()
                .position(dicodingSpace)
                .title("Markas Utama")
                .snippet("Dicoding Space - Jl. Batik Kumeli No.50")
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dicodingSpace, 0f))

        getMyLocation()
        setMapStyle()
        addManyMarker()

    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Cannot find style. Error: ", e)
        }
    }

    private fun addManyMarker() {
        Log.d("MapsActivity", "Start stories...")
        viewModel.stories.observe(this) { result ->
            when (result) {
                is Resource.Loading -> Log.d("MapsActivity", "Loading stories...")
                is Resource.Success -> {
                    Log.d("MapsActivity", "Fetched ${result.data.size} stories")
                    result.data.forEach { data ->
                        val latLng = if (data.lat != null && data.lon != null) LatLng(data.lat, data.lon) else null
                        latLng?.let {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(it)
                                    .title(data.name)
                                    .snippet(data.description)
                            )
                            boundsBuilder.include(it)
                        }
                    }
                }

                is Resource.Error -> {
                    Log.e("MapsActivity", "Error: ${result.error}")
                    showToast(this, "Error loading markers")
                }
            }

        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }

}