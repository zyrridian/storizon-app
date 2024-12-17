package com.example.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.storyapp.data.local.room.StoryDatabase
import com.example.storyapp.data.remote.network.ApiService
import com.example.storyapp.data.remote.request.auth.LoginRequest
import com.example.storyapp.data.remote.request.auth.RegisterRequest
import com.example.storyapp.data.remote.response.Response
import com.example.storyapp.data.remote.response.auth.LoginResponse
import com.example.storyapp.data.remote.response.auth.RegisterResponse
import com.example.storyapp.data.remote.response.story.StoryResponse
import com.example.storyapp.data.remote.response.story.StoryResponseItem
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class StoryRemoteMediatorTest {

    private var mockApi: ApiService = FakeApiService()
    private var mockDb: StoryDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        StoryDatabase::class.java
    ).allowMainThreadQueries().build()

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val remoteMediator = StoryRemoteMediator(
            mockDb,
            mockApi,
            "token"
        )
        val pagingState = PagingState<Int, StoryResponseItem>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @After
    fun tearDown() {
        mockDb.clearAllTables()
    }

}

class FakeApiService : ApiService {
    override suspend fun registerUser(request: RegisterRequest): RegisterResponse {
        TODO("Not yet implemented")
    }

    override suspend fun loginUser(request: LoginRequest): LoginResponse {
        TODO("Not yet implemented")
    }

    override suspend fun addNewStory(
        token: String,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?,
        photo: MultipartBody.Part
    ): Response {
        TODO("Not yet implemented")
    }

    override suspend fun getAllStories(
        token: String,
        page: Int?,
        size: Int?,
        location: Int?
    ): StoryResponse {
        return StoryResponse(
            error = false,
            message = "Stories fetched successfully",
            listStory = List(100) { i ->
                StoryResponseItem(
                    id = i.toString(),
                    name = "Name $i",
                    description = "Description $i",
                    photoUrl = "Photo URL $i",
                    createdAt = "Created At $i",
                    lat = i.toDouble(),
                    lon = i.toDouble()
                )
            }
        )
    }

    override suspend fun getAllStoriesWithLocation(
        token: String,
        page: Int,
        size: Int,
        location: Int
    ): StoryResponse {
        TODO("Not yet implemented")
    }

}