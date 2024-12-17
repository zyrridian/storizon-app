package com.example.storyapp

import com.example.storyapp.data.remote.response.story.StoryResponseItem

object DataDummy {

    fun generateDummyStoryResponse(): List<StoryResponseItem> {
        val items: MutableList<StoryResponseItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoryResponseItem(
                id = i.toString(),
                name = "Name $i",
                description = "Description $i",
                photoUrl = "Photo URL $i",
                createdAt = "Created At $i",
                lat = i.toDouble(),
                lon = i.toDouble()
            )
            items.add(story)
        }
        return items
    }

}
