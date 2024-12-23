package com.example.storyapp.data.local.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storyapp.data.remote.response.story.StoryResponseItem

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    suspend fun getStoriesForWidget(): List<StoryResponseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryResponseItem>)

    @Query("SELECT * FROM stories")
    fun getAllStories(): PagingSource<Int, StoryResponseItem>

    // Fetch all stories ordered by creation date (most recent first)
    @Query("SELECT * FROM stories ORDER BY createdAt DESC LIMIT :limit")
    fun getHomeStories(limit: Int): LiveData<List<StoryResponseItem>>

    @Query("DELETE FROM stories")
    suspend fun deleteAll()
}