package com.calai.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calai.tracker.cactus.CactusEngine
import com.calai.tracker.cactus.CactusStatsOverlay
import com.calai.tracker.ui.theme.CalAiTheme

/**
 * Single Compose host. The Cactus engine telemetry callback is wired once
 * per composition (LaunchedEffect) and the real-time stats overlay reads
 * from a @Composable state. No business logic in onCreate.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalAiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CactusRootScreen()
                }
            }
        }
    }
}

@Composable
private fun CactusRootScreen() {
    var stats by remember { mutableStateOf("Initializing...") }

    // Wire the telemetry callback once per composition. Passing a lambda
    // that captures `stats` makes the callback Compose-aware — every
    // telemetry update flows through Compose state.
    LaunchedEffect(Unit) {
        CactusEngine.setTelemetryCallback { newStats -> stats = newStats }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Cactus AI Local Engine Active",
            modifier = Modifier.padding(16.dp)
        )
        // CactusStatsOverlay has no `modifier` param — wrap in a Box to
        // anchor it to TopEnd. (We could extend the function, but
        // shipping a minimal fix is faster than churning the theme API.)
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            CactusStatsOverlay(stats = stats)
        }
    }
}
