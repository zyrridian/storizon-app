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
import com.example.storyapp.utils.Resource

class StoryViewModel(
    private val repository: StoryRepository
) : ViewModel() {

    private val _token = MutableLiveData<String>()
    private val _size = MutableLiveData<Int>()

    val story: LiveData<PagingData<StoryResponseItem>> = _token.switchMap { token ->
        repository.getStories(token).cachedIn(viewModelScope)
    }

    fun setToken(token: String) {
        _token.value = token
    }

    // LiveData to observe stories
    val homeStories: LiveData<Resource<List<StoryResponseItem>>> = _token.switchMap { token ->
        _size.switchMap { size ->
            repository.getHomeStories(token, size)
        }
    }

    // Set token and size to trigger the fetching of stories
    fun fetchStories(token: String) {
        _token.value = token
    }

    // Set token and size to trigger the fetching of stories
    fun fetchHomeStories(token: String, size: Int) {
        _token.value = token
        _size.value = size
    }

}