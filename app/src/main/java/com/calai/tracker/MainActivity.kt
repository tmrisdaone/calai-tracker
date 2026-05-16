package com.calai.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.calai.tracker.data.api.CalorieApi
import com.calai.tracker.settings.AppSettings
import com.calai.tracker.ui.screens.ScannerScreen
import com.calai.tracker.ui.screens.SettingsScreen
import com.calai.tracker.ui.theme.CalAiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var settings: AppSettings
    private val api = CalorieApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings(this)

        // Load saved config
        runBlocking {
            val baseUrl = settings.apiBaseUrl.first()
            val apiKey = settings.apiKey.first()
            api.updateConfig(baseUrl, apiKey)
        }

        setContent {
            CalAiTheme {
                var currentScreen by remember { mutableStateOf("scanner") }

                // Observe settings changes
                val savedBaseUrl by settings.apiBaseUrl.collectAsState(initial = AppSettings.DEFAULT_API_BASE)
                val savedKey by settings.apiKey.collectAsState(initial = "")

                LaunchedEffect(savedBaseUrl, savedKey) {
                    api.updateConfig(savedBaseUrl, savedKey)
                }

                when (currentScreen) {
                    "scanner" -> ScannerScreen(
                        api = api,
                        onOpenSettings = { currentScreen = "settings" },
                    )
                    "settings" -> SettingsScreen(
                        settings = settings,
                        onBack = { currentScreen = "scanner" },
                    )
                }
            }
        }
    }
}
