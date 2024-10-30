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
import com.example.storyapp.data.remote.response.ErrorResponse
import com.example.storyapp.data.remote.response.auth.LoginResponse
import com.example.storyapp.data.remote.response.auth.RegisterResponse
import com.example.storyapp.data.remote.response.story.StoryResponse
import com.example.storyapp.utils.Resource
import com.example.storyapp.utils.reduceFileImage
import com.example.storyapp.utils.uriToFile
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class StoryRepository(
    private val apiService: ApiService,
    private val storyDao: StoryDao,
) {

    fun registerUser(name: String, email: String, password: String): LiveData<Resource<RegisterResponse>> =
        liveData {
            emit(Resource.Loading)
            try {
                val request = RegisterRequest(name, email, password)
                val response = apiService.registerUser(request)
                if (!response.error) {
                    Log.d("REGISTER", response.toString())
                    emit(Resource.Success(response))
                } else {
                    emit(Resource.Error(response.message))
                }
            } catch (e: HttpException) {
                val errorMessage = e.response()?.errorBody()?.string().let { errorBody ->
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } ?: "An unknown error occurred"
                Log.e("StoryRepository", "registerUser: $errorMessage")
                emit(Resource.Error(errorMessage))
            } catch (e: Exception) {
                Log.e("StoryRepository", "registerUser: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
        }

    fun loginUser(email: String, password: String): LiveData<Resource<LoginResponse>> = liveData {
        emit(Resource.Loading)
        try {
            val request = LoginRequest(email, password)
            val response = apiService.loginUser(request)
            if (!response.error) {
                Log.d("LOGIN", response.toString())
                emit(Resource.Success(response))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string().let { errorBody ->
                Gson().fromJson(errorBody, ErrorResponse::class.java).message
            } ?: "An unknown error occurred"
            Log.e("StoryRepository", "loginUser: $errorMessage")
            emit(Resource.Error(errorMessage))
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

    fun fetchImageForStackWidget(token: String): LiveData<Resource<List<StoryResponse>>> =
        liveData {
            emit(Resource.Loading)
            try {
                val response =
                    apiService.getAllStories("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLVBVc1JDcXdIcFZXM284cGUiLCJpYXQiOjE3MzAxMDk1MTF9.sTvbouekQqTo58Fn_s9loE-3XbE_eJXphojBjkgyjHw")
                val stories = response.listStory
                emit(Resource.Success(stories))
            } catch (e: Exception) {
                Log.e("StoryRepository", "fetchImageForStackWidget: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
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