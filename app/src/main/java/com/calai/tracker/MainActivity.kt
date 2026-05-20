package com.calai.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calai.tracker.cactus.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Cactus Local Engine
        val modelPath = CactusModelManager.ensureModelDownloaded(this)
        val enginePtr = CactusEngine.initEngine(modelPath)
        
        var stats by remember { mutableStateOf("Initializing...") }
        CactusEngine.setTelemetryCallback { newStats ->
            stats = newStats
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // The main app content would go here
                    Text(text = "Cactus AI Local Engine Active", modifier = Modifier.padding(16.dp))
                    
                    // THE REAL-TIME STATS OVERLAY
                    CactusStatsOverlay(
                        stats = stats,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                    )
                }
            }
        }
    }
}
