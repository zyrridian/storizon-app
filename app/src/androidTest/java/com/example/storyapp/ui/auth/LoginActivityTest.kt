package com.example.storyapp.ui.auth

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.storyapp.JsonConverter
import org.junit.Test
import org.junit.runner.RunWith
import com.example.storyapp.R
import com.example.storyapp.data.remote.network.ApiConfig
import com.example.storyapp.utils.EspressoIdlingResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.core.app.ApplicationProvider


//import org.junit.jupiter.api.Assertions.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    private val mockWebServer = MockWebServer()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.BASE_URL = "http://127.0.0.1:8080/"
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginScreen_EmptyEmailAndPassword_ShowsError() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Attempt login without entering anything
        onView(withId(R.id.login_button)).perform(click())

        // Check for error messages
        onView(withId(R.id.ed_login_email))
            .check(matches(hasErrorText(context.getString(R.string.error_empty_email))))
        onView(withId(R.id.ed_login_password))
            .check(matches(hasErrorText(context.getString(R.string.error_empty_password))))
    }

    @Test
    fun loginScreen_EmptyEmail_ShowsError() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Input only password
        onView(withId(R.id.ed_login_password)).perform(typeText("validPassword"), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())

        // Check for email error message
        onView(withId(R.id.ed_login_email))
            .check(matches(hasErrorText(context.getString(R.string.error_empty_email))))
    }

    @Test
    fun loginScreen_EmptyPassword_ShowsError() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Input only email
        onView(withId(R.id.ed_login_email)).perform(typeText("valid@email.com"), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())

        // Check for password error message
        onView(withId(R.id.ed_login_password))
            .check(matches(hasErrorText(context.getString(R.string.error_empty_password))))
    }

    @Test
    fun loginScreen_InvalidEmailFormat_ShowsError() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Input invalid email and valid password
        onView(withId(R.id.ed_login_email)).perform(typeText("invalid-email"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("validPassword"), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())

        // Check for invalid email error message
        onView(withId(R.id.ed_login_email))
            .check(matches(hasErrorText(context.getString(R.string.error_invalid_email))))
    }

    @Test
    fun loginScreen_ShortPassword_ShowsError() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Input valid email and short password
        onView(withId(R.id.ed_login_email)).perform(typeText("valid@email.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("s"), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())

        // Check for short password error message
        onView(withId(R.id.ed_login_password))
            .check(matches(hasErrorText(context.getString(R.string.error_short_password))))
    }

    @Test
    fun loginScreen_ValidInput_ProceedsToLogin() {
        ActivityScenario.launch(LoginActivity::class.java)

        // Input email and password
        onView(withId(R.id.ed_login_email)).perform(typeText("mibnuz@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("mibnuz123"), closeSoftKeyboard())

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("success_response.json"))
        mockWebServer.enqueue(mockResponse)

        // Perform login
        onView(withId(R.id.login_button)).perform(click())

        // Open navigation drawer and go to settings
        onView(withId(R.id.settings_button)).check(matches(isDisplayed()))
        onView(withId(R.id.settings_button)).perform(click())

        // Click logout option
        onView(withId(R.id.logout_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.logout_layout)).perform(click())

        // Handle dialog: Check if dialog title and message are displayed
        onView(withText(R.string.logout)).check(matches(isDisplayed()))
        onView(withText(R.string.are_you_sure_you_want_to_log_out)).check(matches(isDisplayed()))

        // Click "Yes" button in the dialog
        onView(withText(R.string.yes)).check(matches(isDisplayed()))
        onView(withText(R.string.yes)).perform(click())

        // Verify that the app navigates back to the Login screen
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))

    }

}