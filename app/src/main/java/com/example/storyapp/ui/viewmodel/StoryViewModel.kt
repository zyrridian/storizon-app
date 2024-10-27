package com.example.storyapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.utils.Resource
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.data.remote.response.auth.LoginResult
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

    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> = _token

    init {
        viewModelScope.launch {
            _isLoggedIn.value = preferences.getLoginSession().first()
            _token.value = preferences.getTokenSession().firstOrNull()
        }
    }

    fun login(token: String) {
        viewModelScope.launch {
            preferences.saveLoginSession(true)
            preferences.saveTokenSession(token)
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.saveLoginSession(false)
            _isLoggedIn.value = false
        }
    }


    private val _stories = MutableLiveData<Resource<List<StoryEntity>>>()
    val stories: LiveData<Resource<List<StoryEntity>>> = _stories

    fun registerUser(name: String, email: String, password: String): LiveData<Resource<String>> {
        return repository.registerUser(name, email, password)
    }

    fun loginUser(email: String, password: String): LiveData<Resource<LoginResult>> {
        return repository.loginUser(email, password)
    }

    fun addNewUser(uri: Uri, description: String, token: String, context: Context) : LiveData<Resource<String>> {
        return repository.addNewStory(uri, description, token, context)
    }

    fun fetchAllStories(token: String) {
        viewModelScope.launch {
            repository.getAllStories(token).observeForever { result ->
                _stories.postValue(result)
            }
        }
    }

}