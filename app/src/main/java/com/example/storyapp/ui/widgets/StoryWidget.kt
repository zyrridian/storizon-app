@file:Suppress("DEPRECATION")

package com.example.storyapp.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.storyapp.R
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.ui.activities.DetailActivity
import com.example.storyapp.ui.activities.DetailActivity.Companion.EXTRA_STORY

/**
 * Implementation of App Widget functionality.
 */
@Suppress("DEPRECATION")
class StoryWidget : AppWidgetProvider() {

    companion object {
        const val CLICK_ACTION = "com.example.storyapp.ui.widgets.CLICK_ACTION"
        const val EXTRA_STORY_ENTITY = "EXTRA_STORY_ENTITY"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            CLICK_ACTION -> {
                val story = intent.getParcelableExtra<StoryEntity>(EXTRA_STORY_ENTITY)
                if (story != null) {
                    val detailIntent = Intent(context, DetailActivity::class.java).apply {
                        putExtra(EXTRA_STORY, story)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(detailIntent)
                }
            }

            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null) {
                    onUpdate(context, appWidgetManager, appWidgetIds)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}


}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val intent = Intent(context, StackWidgetService::class.java)
    val views = RemoteViews(context.packageName, R.layout.story_widget)
    views.setRemoteAdapter(R.id.stack_view, intent)
    views.setEmptyView(R.id.stack_view, R.id.empty_view)

    val clickIntent = Intent(context, StoryWidget::class.java)
    clickIntent.action = StoryWidget.CLICK_ACTION
    val clickPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        clickIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
    )
    views.setPendingIntentTemplate(R.id.stack_view, clickPendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
