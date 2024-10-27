package com.example.storyapp.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.storyapp.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): LiveData<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isFavorite = 1")
    fun getFavoriteStory(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryEntity>)

    @Update
    fun updateFavoriteStory(story: StoryEntity)

}