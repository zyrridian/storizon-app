package com.example.storyapp.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivitySettingsBinding
import com.example.storyapp.ui.SettingsPreferences
import com.example.storyapp.ui.dataStore
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this@SettingsActivity)
    }

    val settingsPreferences = SettingsPreferences.getInstance(dataStore)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        loadSettingsData()

        viewModel.getThemeSettings().observe(this) {
            binding.darkModeSwitch.isChecked = it
        }

    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.logoutLayout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.languageLayout.setOnClickListener { showLanguageSelectionDialog() }
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isDark ->
            viewModel.saveThemeSetting(isDark)
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun loadSettingsData() {
        lifecycleScope.launch {
            settingsPreferences.getUserName().collect { name ->
                binding.nameTextView.text = name ?: "Guest"
            }
        }
        lifecycleScope.launch {
            settingsPreferences.getUserEmail().collect { email ->
                binding.emailTextView.text = email ?: "No email provided"
            }
        }
        lifecycleScope.launch {
            settingsPreferences.getThemeSetting().collect { isDarkMode ->
                binding.darkModeSwitch.isChecked = isDarkMode
            }
        }
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Bahasa Indonesia", "русский язык")
        lifecycleScope.launch {
            val savedLanguage =
                settingsPreferences.getLanguageSetting().firstOrNull() ?: "English"
            var selectedLanguage = languages.indexOf(savedLanguage).takeIf { it >= 0 } ?: 0

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, selectedLanguage) { _, which ->
                    selectedLanguage = which
                }
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val chosenLanguage = languages[selectedLanguage]
                    saveLanguageSetting(chosenLanguage)
                    applyLanguage(chosenLanguage)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun saveLanguageSetting(language: String) {
        lifecycleScope.launch {
            settingsPreferences.saveLanguageSetting(language)
        }
    }

    private fun applyLanguage(language: String) {
        val locale = when (language) {
            "Bahasa Indonesia" -> Locale("in")
            "русский язык" -> Locale("ru")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.are_you_sure_you_want_to_log_out))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.logout()
                startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}