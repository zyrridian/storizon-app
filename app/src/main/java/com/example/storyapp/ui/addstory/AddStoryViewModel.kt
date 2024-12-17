package com.example.storyapp.ui.addstory

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.utils.Resource

class AddStoryViewModel(
    private val repository: StoryRepository,
) : ViewModel() {
    fun addNewStory(
        uri: Uri,
        description: String,
        lat: Double?,
        lon: Double?,
        token: String,
        context: Context
    ): LiveData<Resource<String>> {
        return repository.addNewStory(uri, description, lat, lon, token, context)
    }
}