package com.calai.tracker.cactus

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun CactusStatsOverlay(stats: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Text(
            text = "Cactus Local Stats: $stats",
            color = androidx.compose.ui.graphics.Color.Green,
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
