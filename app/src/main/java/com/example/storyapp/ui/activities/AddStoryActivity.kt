package com.example.storyapp.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
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
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory
import com.example.storyapp.ui.activities.CameraActivity.Companion.CAMERAX_RESULT

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: StoryViewModel by viewModels() {
        ViewModelFactory.getInstance(this)
    }
    private var currentImageUri: Uri? = null
    private var imageCapture: ImageCapture? = null

    private lateinit var token: String

//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            Toast.makeText(this, "Permission request granted", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Permission request denied", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun allPermissionGranted() =
//        ContextCompat.checkSelfPermission(
//            this,
//            REQUIRED_PERMISSION
//        ) == PackageManager.PERMISSION_GRANTED

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

//        if (!allPermissionGranted()) {
//            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
//        }

        viewModel.token.observe(this) {
            if (it != null) {
                token = "Bearer $it"
                setup()
            }
        }

    }

    private fun setup() {
        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCameraX() }
            uploadButton.setOnClickListener { uploadImage() }
        }
    }


    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }


    private fun uploadImage() {
        val description = binding.descriptionEditText.text.toString()
        currentImageUri?.let {
            viewModel.addNewUser(
                uri = it,
                description = description,
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


    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imageView.setImageURI(it)
        }
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }
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
        savedInstanceState.getString("CURRENT_IMAGE_URI")?.let {
            currentImageUri = Uri.parse(it)
            showImage()
        }
    }



//    companion object {
//        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
//    }


    private fun showLoading() {
        // Show a loading indicator
    }

    private fun hideLoading() {
        // Hide the loading indicator
    }

}