package com.example.storyapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.utils.Resource
import kotlinx.coroutines.launch

class MapsViewModel(
    private val repository: StoryRepository,
) : ViewModel() {

    private val _stories = MutableLiveData<Resource<List<StoryResponseItem>>>()
    val stories: LiveData<Resource<List<StoryResponseItem>>> get() = _stories

    fun fetchStoriesWithLocation(token: String) {
        viewModelScope.launch {
            repository.getAllStoriesWithLocation(token).observeForever {
                _stories.postValue(it)
            }
        }

    }


}