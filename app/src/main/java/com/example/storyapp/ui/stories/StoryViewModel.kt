package com.example.storyapp.ui.stories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.lifecycle.switchMap
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.ui.SettingsPreferences

class StoryViewModel(
    val repository: StoryRepository
//    private val settingsPreferences: SettingsPreferences
) : ViewModel() {


    private val _token = MutableLiveData<String>()

//    val story: LiveData<PagingData<StoryResponseItem>> =
//        repository.getStories("Bearer $token").cachedIn(viewModelScope)

    val story: LiveData<PagingData<StoryResponseItem>> = _token.switchMap { token ->
        repository.getStories(token).cachedIn(viewModelScope)
    }

    fun setToken(token: String) {
        _token.value = token
    }

}