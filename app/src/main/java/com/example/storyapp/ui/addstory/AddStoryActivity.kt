package com.example.storyapp.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.addstory.CameraActivity.Companion.CAMERAX_RESULT
import com.example.storyapp.ui.dataStore
import com.example.storyapp.utils.showToast
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var token: String

    private var currentImageUri: Uri? = null
    private var imageCapture: ImageCapture? = null
    private var lat: Double? = null
    private var lon: Double? = null

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val viewModel: AddStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchLocation()
            } else {
                showToast(this, "Location Permission Denied")
            }
        }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        currentImageUri = uri
        showImage()
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            token = "Bearer ${SettingsPreferences.getInstance(dataStore).getTokenSession().first()}"
        }

        binding.apply {
            backButton.setOnClickListener { finish() }
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCameraX() }
            buttonAdd.setOnClickListener { uploadStory() }
            locationCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkAndRequestLocationPermission()
                } else { // Reset location value if unchecked
                    lat = null
                    lon = null
                }
            }
        }

    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user asynchronously
                showToast(this,
                    getString(R.string.location_permission_is_required_to_provide_better_service))
                openAppSettings()
            }

            else -> {
                // Directly request the permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                } else {
                    showToast(this, getString(R.string.unable_to_fetch_location))
                }
            }.addOnFailureListener {
                showToast(this, getString(R.string.location_fetch_failed))
            }
        } else {
            showToast(this, getString(R.string.location_permission_not_granted))
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private fun uploadStory() {
        if (!validation()) return
        currentImageUri?.let { uri ->
            viewModel.addNewStory(
                uri = uri,
                description = binding.edAddDescription.text.toString(),
                lat = lat,
                lon = lon,
                token = token,
                context = this
            ).observe(this) { result ->
                when (result) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success -> {
                        hideLoading()
                        onBackPressedDispatcher.onBackPressed()
                    }

                    is Resource.Error -> hideLoading()
                }
            }
        }
    }

    private fun showImage() {
        currentImageUri?.let { binding.imageView.setImageURI(it) }
    }

    private fun validation(): Boolean {
        var isValid = true

        if (currentImageUri == null) {
            isValid = false
            showToast(this, getString(R.string.toast_please_select_an_image))
        }

        val description = binding.edAddDescription.text.toString()
        if (description.isEmpty()) {
            binding.edAddDescription.error = getString(R.string.error_description_cannot_be_empty)
            isValid = false
            showToast(this, getString(R.string.toast_please_add_a_description))
        }

        return isValid
    }


    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CURRENT_IMAGE_URI", currentImageUri?.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentImageUri = savedInstanceState.getString("CURRENT_IMAGE_URI")?.toUri()
        showImage()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

}