package com.example.storyapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
//import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.data.local.room.StoryDao
import com.example.storyapp.data.local.room.StoryDatabase
import com.example.storyapp.data.remote.network.ApiService
import com.example.storyapp.data.remote.request.auth.LoginRequest
import com.example.storyapp.data.remote.request.auth.RegisterRequest
import com.example.storyapp.data.remote.response.ErrorResponse
import com.example.storyapp.data.remote.response.auth.LoginResponse
import com.example.storyapp.data.remote.response.auth.RegisterResponse
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.utils.Resource
import com.example.storyapp.utils.reduceFileImage
import com.example.storyapp.utils.uriToFile
import com.example.storyapp.utils.wrapEspressoIdlingResource
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class StoryRepository(
    val database: StoryDatabase,
    val apiService: ApiService,
    val storyDao: StoryDao,
) {

    fun registerUser(
        name: String,
        email: String,
        password: String
    ): LiveData<Resource<RegisterResponse>> =
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
                }
                Log.e("StoryRepository", "registerUser: $errorMessage")
                emit(Resource.Error(errorMessage))
            } catch (e: Exception) {
                Log.e("StoryRepository", "registerUser: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
        }

    fun loginUser(email: String, password: String): LiveData<Resource<LoginResponse>> = liveData {
        emit(Resource.Loading)
        wrapEspressoIdlingResource {
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
                }
                Log.e("StoryRepository", "loginUser: $errorMessage")
                emit(Resource.Error(errorMessage))
            } catch (e: Exception) {
                Log.e("StoryRepository", "loginUser: ${e.message}")
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }

    fun addNewStory(
        uri: Uri,
        description: String,
        lat: Double?,
        lon: Double?,
        token: String,
        context: Context
    ): LiveData<Resource<String>> =
        liveData {
            emit(Resource.Loading)
            try {
                val imageFile = uriToFile(uri, context).reduceFileImage()
                val descriptionBody = description.toRequestBody("text/plain".toMediaType())
                val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
                val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo", imageFile.name, requestImageFile
                )
                val response =
                    apiService.addNewStory(token, descriptionBody, latBody, lonBody, multipartBody)
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

    fun getStories(token: String): LiveData<PagingData<StoryResponseItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(database, apiService, token),
            pagingSourceFactory = {
                database.storyDao().getAllStories()
            }
        ).liveData
    }

    fun getAllStoriesWithLocation(token: String): LiveData<Resource<List<StoryResponseItem>>> =
        liveData {
            emit(Resource.Loading)
            try {
                val response = apiService.getAllStoriesWithLocation(token, location = 1)
                emit(Resource.Success(response.listStory))
            } catch (e: Exception) {
                Log.d("StoryRepository", "getStory: ${e.message.toString()} ")
                emit(Resource.Error(e.message.toString()))
            }
        }

    suspend fun getLocalStories(): List<StoryResponseItem> = storyDao.getStoriesForWidget()

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            database: StoryDatabase,
            apiService: ApiService,
            storyDao: StoryDao,
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(
                database,
                apiService,
                storyDao,
            )
        }.also { instance = it }
    }

}