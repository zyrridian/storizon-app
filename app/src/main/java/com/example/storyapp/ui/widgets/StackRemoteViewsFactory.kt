package com.example.storyapp.ui.widgets

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.storyapp.R
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import com.example.storyapp.di.Injection
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking

class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val storyList = mutableListOf<StoryResponseItem>()
    private val storyRepository: StoryRepository = Injection.provideRepository(mContext)

    override fun onCreate() {
        runBlocking {
            val result = storyRepository.getLocalStories()
            storyList.clear()
            storyList.addAll(result)
        }
    }

    override fun onDataSetChanged() {
        runBlocking {
            val result = storyRepository.getLocalStories()
            storyList.clear()
            storyList.addAll(result)
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        if (position == -1 || position >= storyList.size) {
            return RemoteViews(mContext.packageName, R.layout.widget_item).apply {
                setImageViewResource(R.id.widget_image, R.drawable.ic_image_placeholder)
            }
        }

        val story = storyList[position]
        val views = RemoteViews(mContext.packageName, R.layout.widget_item)

        try {
            val bitmap = Picasso.get().load(story.photoUrl).get()
            views.setImageViewBitmap(R.id.widget_image, bitmap)
        } catch (e: Exception) {
            Log.e("StoryRemoteViewsFactory", "Error loading image: ${e.message}")
            views.setImageViewResource(R.id.widget_image, R.drawable.ic_image_placeholder)
        }

        // Set up the click handler for the widget item
        val fillInIntent = Intent().apply {
            action = StoryWidget.CLICK_ACTION
            putExtra(StoryWidget.EXTRA_STORY_ENTITY, story)
        }

        Log.e("StoryRemoteViewsFactory", "Error loading image: ${story.photoUrl}")
        views.setOnClickFillInIntent(R.id.widget_image, fillInIntent)

        return views

    }

    override fun getCount(): Int = storyList.size

    override fun onDestroy() {
        storyList.clear()
    }

    override fun getItemId(position: Int): Long = 0

    override fun hasStableIds(): Boolean = true

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

}