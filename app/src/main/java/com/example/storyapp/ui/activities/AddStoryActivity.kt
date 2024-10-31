package com.example.storyapp.ui.activities

import android.content.Intent
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
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory
import com.example.storyapp.ui.activities.CameraActivity.Companion.CAMERAX_RESULT

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var token: String
    private var currentImageUri: Uri? = null
    private var imageCapture: ImageCapture? = null
    private val viewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
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

        setupListeners()

        viewModel.token.observe(this) {
            token = "Bearer $it"
        }

    }

    private fun setupListeners() {
        binding.apply {
            backButton.setOnClickListener { finish() }
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCameraX() }
            buttonAdd.setOnClickListener { uploadImage() }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            viewModel.addNewStory(
                uri = uri,
                description = binding.edAddDescription.text.toString(),
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

}