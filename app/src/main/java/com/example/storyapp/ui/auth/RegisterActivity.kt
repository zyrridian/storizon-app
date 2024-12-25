package com.example.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storyapp.R
import com.example.storyapp.utils.Resource
import com.example.storyapp.databinding.ActivityAuthRegisterBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.utils.showToast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory.getInstance(this@RegisterActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playAnimation()
        setupButtonListeners()

    }

    private fun playAnimation() {
        val animations = listOf(
            ObjectAnimator.ofFloat(binding.imageView, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.headerTextView, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.subHeaderTextView, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.nameTextInputLayout, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.emailTextInputLayout, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.passwordTextInputLayout, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.confirmPasswordTextInputLayout, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(200),
            ObjectAnimator.ofFloat(binding.loginLayout, View.ALPHA, 1f).setDuration(200)
        )

        AnimatorSet().apply {
            playSequentially(*animations.toTypedArray())
            start()
        }
    }

    private fun setupButtonListeners() {
        binding.apply {
            registerButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()
                validateInputs(name, email, password, confirmPassword)
            }
            loginButton.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String) {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditText.error = getString(R.string.error_empty_name)
            isValid = false
        }
        if (email.isEmpty()) {
            binding.emailEditText.error = getString(R.string.error_empty_email)
            isValid = false
        }
        if (password.isEmpty()) {
            binding.passwordEditText.error = getString(R.string.error_empty_password)
            isValid = false
        }
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = getString(R.string.error_empty_password)
            isValid = false
        }

        if (isValid) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = getString(R.string.error_invalid_email)
                isValid = false
            }
            if (password.length < 8) {
                binding.passwordEditText.error = getString(R.string.error_short_password)
                isValid = false
            }
            if (password != confirmPassword) {
                binding.confirmPasswordEditText.error = getString(R.string.error_password_not_match)
                isValid = false
            }
        }

        if (isValid) {
            register(name, email, password)
        } else {
            showToast(this, getString(R.string.error_fill_all_fields))
        }
    }

    private fun register(name: String, email: String, password: String) {
        viewModel.registerUser(name, email, password).observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    showToast(this, getString(R.string.toast_account_created_successfully))
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }

                is Resource.Error -> {
                    hideLoading()
                    showToast(this, result.error)
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

}