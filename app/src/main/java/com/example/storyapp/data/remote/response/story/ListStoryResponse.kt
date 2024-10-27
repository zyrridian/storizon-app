package com.example.storyapp.data.remote.response.story

import com.google.gson.annotations.SerializedName

data class ListStoryResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("listStory")
    val listStory: List<StoryResponse>

)