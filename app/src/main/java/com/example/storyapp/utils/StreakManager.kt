package com.example.storyapp.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object StreakManager {
    private const val PREF_NAME = "LoginStreakPrefs"
    private const val KEY_LAST_LOGIN_DATE = "last_login_date"
    private const val KEY_STREAK_COUNT = "streak_count"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateLoginStreak(context: Context): Int {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val lastLoginDate = sharedPreferences.getString(KEY_LAST_LOGIN_DATE, null)
        val streakCount = sharedPreferences.getInt(KEY_STREAK_COUNT, 0)

        val today = Calendar.getInstance()
        val todayDate = dateFormat.format(today.time)

        val updatedStreakCount = if (lastLoginDate != null) {
            val lastLoginCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(lastLoginDate)!!
            }

            val differenceInDays = getDifferenceInDays(lastLoginCalendar, today)

            when {
                differenceInDays == 1 -> streakCount + 1 // Increment streak
                differenceInDays > 1 -> 1                // Reset streak
                else -> streakCount                      // Same day, no change
            }
        } else {
            1 // First login
        }

        // Save the updated values
        sharedPreferences.edit()
            .putString(KEY_LAST_LOGIN_DATE, todayDate)
            .putInt(KEY_STREAK_COUNT, updatedStreakCount)
            .apply()

        return updatedStreakCount
    }

    private fun getDifferenceInDays(startDate: Calendar, endDate: Calendar): Int {
        val startDay = startDate.get(Calendar.DAY_OF_YEAR)
        val endDay = endDate.get(Calendar.DAY_OF_YEAR)
        val startYear = startDate.get(Calendar.YEAR)
        val endYear = endDate.get(Calendar.YEAR)

        return if (startYear == endYear) {
            endDay - startDay
        } else {
            val daysInStartYear = startDate.getActualMaximum(Calendar.DAY_OF_YEAR)
            (endDay + daysInStartYear - startDay)
        }
    }

    fun resetStreak(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("LoginStreakPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(KEY_LAST_LOGIN_DATE, null)
            .putInt(KEY_STREAK_COUNT, 0)
            .apply()
    }
}
