package com.calai.tracker.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calai_settings")

class AppSettings(private val context: Context) {

    companion object {
        val DEFAULT_API_BASE = "https://api.openai.com/v1"
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_BASE_KEY] ?: DEFAULT_API_BASE
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_KEY_KEY] ?: ""
    }

    suspend fun saveApiBaseUrl(url: String) {
        context.dataStore.edit { it[API_BASE_KEY] = url }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[API_KEY_KEY] = key }
    }

    private val API_BASE_KEY = stringPreferencesKey("api_base_url")
    private val API_KEY_KEY = stringPreferencesKey("api_key")
}
