package com.example.storyapp.data.local.entity

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// @Entity(tableName = "stories")
@Parcelize
data class StoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double?,
    val lon: Double?,
    var isFavorite: Boolean = false
) : Parcelable