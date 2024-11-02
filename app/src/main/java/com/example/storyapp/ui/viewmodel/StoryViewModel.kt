package com.example.storyapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.storyapp.utils.Resource
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.data.remote.response.auth.LoginResponse
import com.example.storyapp.data.remote.response.auth.RegisterResponse
import com.example.storyapp.ui.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StoryViewModel(
    private val repository: StoryRepository,
    private val preferences: SettingsPreferences
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _name = MutableLiveData<String?>()
    val name: LiveData<String?> = _name

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> = _token

    private val _stories = MutableLiveData<Resource<List<StoryEntity>>>()
    val stories: LiveData<Resource<List<StoryEntity>>> = _stories

    init {
        viewModelScope.launch {
            _isLoggedIn.value = preferences.getLoginSession().first()
            _token.value = preferences.getTokenSession().firstOrNull()
        }
    }

    // Get stories
    fun fetchAllStories(token: String) {
        viewModelScope.launch {
            repository.getAllStories(token).observeForever { result ->
                _stories.postValue(result)
            }
        }
    }

    // Add story
    fun addNewStory(
        uri: Uri,
        description: String,
        token: String,
        context: Context
    ): LiveData<Resource<String>> {
        return repository.addNewStory(uri, description, token, context)
    }

    // Theme functions
    fun getThemeSettings() = preferences.getThemeSetting().asLiveData()
    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            preferences.saveThemeSetting(isDarkModeActive)
        }
    }

    // Save user login session
    fun saveUserLoginSession(name: String, email: String, token: String) {
        viewModelScope.launch {
            preferences.saveUserName(name)
            preferences.saveUserEmail(email)
            preferences.saveLoginSession(true)
            preferences.saveTokenSession(token)
            _isLoggedIn.value = true
        }
    }

    // Register user
    fun registerUser(
        name: String,
        email: String,
        password: String
    ): LiveData<Resource<RegisterResponse>> {
        return repository.registerUser(name, email, password)
    }

    // Login user
    fun loginUser(email: String, password: String): LiveData<Resource<LoginResponse>> {
        return repository.loginUser(email, password)
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            preferences.saveLoginSession(false)
            _isLoggedIn.value = false
        }
    }

}