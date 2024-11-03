package com.example.storyapp.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsPreferencesKeys {
    val NAME = stringPreferencesKey("user_name")
    val EMAIL = stringPreferencesKey("user_email")
}

class SettingsPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val nameKey = SettingsPreferencesKeys.NAME
    private val emailKey = SettingsPreferencesKeys.EMAIL

    private val themeKey = booleanPreferencesKey("theme_setting")
    private val languageKey = stringPreferencesKey("language_key")
    private val loginKey = booleanPreferencesKey("login_key")
    private val tokenKey = stringPreferencesKey("token_key")

    // Save user name
    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[nameKey] = name
        }
    }

    // Get user name
    fun getUserName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[nameKey]
        }
    }

    // Save user email
    suspend fun saveUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[emailKey] = email
        }
    }

    // Get user email
    fun getUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[emailKey]
        }
    }

    // login
    suspend fun saveLoginSession(isLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[loginKey] = isLogin
        }
    }

    fun getLoginSession(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[loginKey] ?: false
        }
    }

    // token
    suspend fun saveTokenSession(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    fun getTokenSession() : Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[tokenKey] ?: ""
        }
    }

    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[themeKey] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[themeKey] = isDarkModeActive
        }
    }

    fun getLanguageSetting(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[languageKey] ?: ""
        }
    }

    suspend fun saveLanguageSetting(language: String) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }

    // todo: notification

    suspend fun deleteSessionName () {
        dataStore.edit { preferences ->
            preferences.remove(nameKey)
        }
    }

    suspend fun deleteSessionEmail () {
        dataStore.edit { preferences ->
            preferences.remove(emailKey)
        }
    }

    suspend fun deleteSessionToken () {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }

    suspend fun deleteSessionLogin() {
        dataStore.edit { preferences ->
            preferences.remove(loginKey)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsPreferences? = null
        fun getInstance(dataStore: DataStore<Preferences>): SettingsPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}