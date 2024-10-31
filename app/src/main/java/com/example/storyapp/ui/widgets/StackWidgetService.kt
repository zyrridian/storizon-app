package com.example.storyapp.ui.widgets

import android.content.Intent
import android.widget.RemoteViewsService
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.example.storyapp.ui.viewmodel.ViewModelFactory

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext)
//        val viewModelFactory = ViewModelFactory.getInstance(applicationContext)
//        val viewModel = viewModelFactory.create(StoryViewModel::class.java)
//        return StackRemoteViewsFactory(this.applicationContext, viewModel)
    }
}