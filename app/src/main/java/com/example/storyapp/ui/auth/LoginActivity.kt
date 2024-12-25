package com.example.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityAuthLoginBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.dataStore
import com.example.storyapp.ui.stories.StoryActivity
import com.example.storyapp.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory.getInstance(this@LoginActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        applySavedLanguage()
        observeThemeSetting()
        observeLoginStatus()
    }

    private fun observeThemeSetting() {
        viewModel.getThemeSettings().observe(this) { isDarkMode ->
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun observeLoginStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedIn.collectLatest { loggedIn ->
                    if (loggedIn) navigateToMain() else setupUI()
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this@LoginActivity, StoryActivity::class.java))
        finish()
    }

    private fun setupUI() {
        enableEdgeToEdge()
        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
        playAnimation()
        setupButtonListeners()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @SuppressLint("AppBundleLocaleChanges")
    private fun applySavedLanguage() {
        lifecycleScope.launch {
            val savedLanguage =
                SettingsPreferences.getInstance(dataStore).getLanguageSetting().firstOrNull()
                    ?: "English"
            val locale = when (savedLanguage) {
                "Bahasa Indonesia" -> Locale("id")
                "русский язык" -> Locale("ru")
                else -> Locale("en")
            }
            Locale.setDefault(locale)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    private fun playAnimation() {
        val animations = AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.imageView, View.ALPHA, 1f).setDuration(200),
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.headerTextView, View.ALPHA, 1f)
                            .setDuration(200),
                        ObjectAnimator.ofFloat(binding.subHeaderTextView, View.ALPHA, 1f)
                            .setDuration(200)
                    )
                },
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.emailTextInputLayout, View.ALPHA, 1f)
                            .setDuration(200),
                        ObjectAnimator.ofFloat(binding.passwordTextInputLayout, View.ALPHA, 1f)
                            .setDuration(200)
                    )
                },
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f)
                            .setDuration(200),
                        ObjectAnimator.ofFloat(binding.registerLayout, View.ALPHA, 1f)
                            .setDuration(200)
                    )
                }
            )
        }
        animations.start()
    }

    private fun setupButtonListeners() {
        binding.apply {
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                validateCredentials(email, password)
            }
            registerButton.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        }
    }

    private fun validateCredentials(email: String, password: String) {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailEditText.error = getString(R.string.error_empty_email)
            isValid = false
        }
        if (password.isEmpty()) {
            binding.passwordEditText.error = getString(R.string.error_empty_password)
            isValid = false
        }
        if (isValid && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = getString(R.string.error_invalid_email)
            isValid = false
        }
        if (isValid && password.length < 8) {
            binding.passwordEditText.error = getString(R.string.error_short_password)
            isValid = false
        }
        if (isValid) login(
            email,
            password
        ) else showToast(getString(R.string.error_fill_all_fields))
    }

    private fun login(email: String, password: String) {
        viewModel.loginUser(email, password).observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    showToast("Welcome, ${result.data.loginResult.name}")
                    viewModel.saveUserLoginSession(
                        result.data.loginResult.name,
                        email,
                        result.data.loginResult.token
                    )
                }

                is Resource.Error -> {
                    hideLoading()
                    showToast(result.error)
                }
            }
        }
    }


    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}