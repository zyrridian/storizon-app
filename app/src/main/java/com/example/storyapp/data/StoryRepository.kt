package com.example.storyapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.data.local.room.StoryDao
import com.example.storyapp.data.remote.network.ApiService
import com.example.storyapp.data.remote.request.auth.LoginRequest
import com.example.storyapp.data.remote.request.auth.RegisterRequest
import com.example.storyapp.data.remote.response.auth.LoginResult
import com.example.storyapp.utils.Resource
import com.example.storyapp.utils.reduceFileImage
import com.example.storyapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class StoryRepository(
    private val apiService: ApiService,
    private val storyDao: StoryDao,
) {

    fun registerUser(name: String, email: String, password: String): LiveData<Resource<String>> =
        liveData {
            emit(Resource.Loading)
            try {
                val request = RegisterRequest(name, email, password)
                val response = apiService.registerUser(request)
                if (!response.error) {
                    emit(Resource.Success(response.message))
                } else {
                    emit(Resource.Error("Registration Failed"))
                }
            } catch (e: Exception) {
                Log.e("StoryRepository", "registerUser: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
        }

    fun loginUser(email: String, password: String): LiveData<Resource<LoginResult>> = liveData {
        emit(Resource.Loading)
        try {
            val request = LoginRequest(email, password)
            val response = apiService.loginUser(request)
            if (!response.error) {
                Log.d("LOGIN", response.loginResult.toString())
                emit(Resource.Success(response.loginResult))
            } else {
                emit(Resource.Error("Login Failed"))
            }
        } catch (e: Exception) {
            Log.e("StoryRepository", "loginUser: ${e.message}")
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun addNewStory(
        uri: Uri,
        description: String,
        token: String,
        context: Context
    ): LiveData<Resource<String>> =
        liveData {
            emit(Resource.Loading)
            try {
                val imageFile = uriToFile(uri, context).reduceFileImage()
                val requestBody = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo", imageFile.name, requestImageFile
                )
                val response = apiService.addNewStory(token, requestBody, multipartBody)
                if (!response.error) {
                    emit(Resource.Success(response.message))
                } else {
                    emit(Resource.Error("Add New Story Failed"))
                }
            } catch (e: Exception) {
                Log.e("StoryRepository", "addNewStory: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
        }

    fun getAllStories(token: String): LiveData<Resource<List<StoryEntity>>> = liveData {
        emit(Resource.Loading)
        try {
            val response = apiService.getAllStories(token)
            val stories = response.listStory.map { story ->
                StoryEntity(
                    id = story.id,
                    name = story.name,
                    description = story.description,
                    photoUrl = story.photoUrl,
                    createdAt = story.createdAt,
                    lat = story.lat,
                    lon = story.lon,
                )
            }
            storyDao.insertStory(stories)
        } catch (e: Exception) {
            Log.d("StoryRepository", "getStory: ${e.message.toString()} ")
            emit(Resource.Error(e.message.toString()))
        }

        // Save to room
        val localData: LiveData<Resource<List<StoryEntity>>> =
            storyDao.getAllStories().map { stories ->
                Resource.Success(stories.sortedByDescending { it.createdAt })
            }
        emitSource(localData)
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDao: StoryDao,
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(
                apiService,
                storyDao,
            )
        }.also { instance = it }
    }

}