package com.example.storyapp.data.remote.network

import com.example.storyapp.data.remote.request.auth.LoginRequest
import com.example.storyapp.data.remote.request.auth.RegisterRequest
import com.example.storyapp.data.remote.response.story.ListStoryResponse
import com.example.storyapp.data.remote.response.Response
import com.example.storyapp.data.remote.response.auth.LoginResponse
import com.example.storyapp.data.remote.response.auth.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addNewStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null // Default to 0 if not provided
    ): ListStoryResponse

}