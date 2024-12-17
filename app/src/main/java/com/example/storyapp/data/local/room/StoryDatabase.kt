package com.example.storyapp.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.storyapp.data.local.RemoteKeys
import com.example.storyapp.data.local.RemoteKeysDao
//import com.example.storyapp.data.local.entity.StoryEntity
//import com.example.storyapp.data.remote.response.story.StoryResponse
import com.example.storyapp.data.remote.response.story.StoryResponseItem

@Database(
    entities = [
        StoryResponseItem::class,
        RemoteKeys::class
    ],
    version = 5,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null
        @JvmStatic
        fun getInstance(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "Story.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}