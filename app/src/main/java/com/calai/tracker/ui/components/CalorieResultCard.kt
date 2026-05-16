package com.calai.tracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.tracker.data.model.CalorieResponse
import com.calai.tracker.ui.theme.*

@Composable
fun CalorieResultCard(
    response: CalorieResponse,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Dismiss", tint = TextMuted)
                }
            }

            // Food name
            Text(
                text = response.food_name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = response.serving_size,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )

            Spacer(Modifier.height(16.dp))

            // Big calorie number
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(70.dp))
                    .background(
                        Brush.sweepGradient(listOf(Emerald, EmeraldDark, CalBlue, Emerald))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${response.calories.toInt()}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "kcal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Macro bars
            MacroRow("Protein", "${response.protein_g.toInt()}g", response.protein_g, CalGreen)
            MacroRow("Carbs", "${response.carbs_g.toInt()}g", response.carbs_g, CalOrange)
            MacroRow("Fat", "${response.fat_g.toInt()}g", response.fat_g, CalRed)
            MacroRow("Fiber", "${response.fiber_g.toInt()}g", response.fiber_g, CalBlue)

            // Confidence
            if (response.confidence > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Confidence: ${(response.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }

            // Breakdown
            if (response.breakdown.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = response.breakdown,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    value: String,
    grams: Double,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (grams / 50.0).coerceIn(0.0, 1.0).toFloat())
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
