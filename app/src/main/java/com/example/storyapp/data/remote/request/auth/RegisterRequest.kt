package com.example.storyapp.data.remote.request.auth

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)