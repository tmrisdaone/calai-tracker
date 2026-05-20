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
        val DEFAULT_API_BASE = "http://localhost:11434/api"
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_BASE_KEY] ?: DEFAULT_API_BASE
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_KEY_KEY] ?: ""
    }

    val modelName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[MODEL_NAME_KEY] ?: "gpt-4o-mini"
    }

    val localModelPath: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LOCAL_MODEL_PATH_KEY]
    }

    suspend fun saveApiBaseUrl(url: String) {
        context.dataStore.edit { it[API_BASE_KEY] = url }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[API_KEY_KEY] = key }
    }

    suspend fun saveModelName(name: String) {
        context.dataStore.edit { it[MODEL_NAME_KEY] = name }
    }

    suspend fun saveLocalModelPath(path: String?) {
        context.dataStore.edit { 
            if (path == null) it.remove(LOCAL_MODEL_PATH_KEY) 
            else it[LOCAL_MODEL_PATH_KEY] = path 
        }
    }

    private val API_BASE_KEY = stringPreferencesKey("api_base_url")
    private val API_KEY_KEY = stringPreferencesKey("api_key")
    private val MODEL_NAME_KEY = stringPreferencesKey("model_name")
    private val LOCAL_MODEL_PATH_KEY = stringPreferencesKey("local_model_path")
}
