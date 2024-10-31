package com.example.storyapp.ui.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.example.storyapp.R

internal class StackRemoteViewsFactory(
    private val mContext: Context,
//    private val viewModel: StoryViewModel
) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<Bitmap>()
//    private var stories: List<StoryEntity> = listOf()

    override fun onCreate() {
//        // Load local stories once widget is created
//        viewModel.localStories.observeForever {
//            stories = it ?: listOf()
//            updateWidget()
//        }
    }

    override fun onDataSetChanged() {
        //Ini berfungsi untuk melakukan refresh saat terjadi perubahan.
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.darth_vader))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.star_wars_logo))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.storm_trooper))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.starwars))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.falcon))
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.widget_image, mWidgetItems[position])

        val extras = bundleOf(
            StoryWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.widget_image, fillInIntent)
        return rv
//        if (stories.isEmpty()) return null
//
//        val story = stories[position]
//        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
//
//        // Populate the widget with data from the StoryEntity
//        rv.setTextViewText(R.id.story_title, story.name)
//        rv.setTextViewText(R.id.story_description, story.description)
//        rv.setImageViewUri(R.id.widget_image, Uri.parse(story.photoUrl))
//        Log.d("ERORRRRRRRRRRRRRRRRRRRRRR", story.photoUrl)
//
//        return rv
    }

    override fun getCount(): Int = mWidgetItems.size//stories.size

//    private fun updateWidget() {
//        val intent = Intent(mContext, StoryWidget::class.java).apply {
//            action = StoryWidget.UPDATE_WIDGET_ACTION
//        }
//        mContext.sendBroadcast(intent)
//    }

    override fun onDestroy() {}

    override fun getItemId(position: Int): Long = 0//position.toLong()

    override fun hasStableIds(): Boolean = false //true

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

}