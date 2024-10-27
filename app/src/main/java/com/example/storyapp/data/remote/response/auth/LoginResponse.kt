package com.example.storyapp.data.remote.response.auth

data class LoginResponse(
    val message: String,
    val error: Boolean,
    val loginResult: LoginResult
)

data class LoginResult(
    val userId: String,
    val name: String,
    val token: String
)
