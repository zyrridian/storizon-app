package com.example.storyapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import com.example.storyapp.R
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory

class SettingsFragment : PreferenceFragmentCompat() {

//    private lateinit var workManager: WorkManager
//    private lateinit var periodicWorkRequest: PeriodicWorkRequest

    private val viewModel by viewModels<StoryViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }


}