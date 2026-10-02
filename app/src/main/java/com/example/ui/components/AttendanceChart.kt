package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarChartEntry(val label: String, val value: Float, val color: Color)

@Composable
fun AttendanceBarChart(
    title: String,
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No attendance data available", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                val maxValue = (entries.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val barWidth = (size.width / (entries.size * 2))
                    val spaceBetween = barWidth
                    val maxHeight = size.height - 30f

                    entries.forEachIndexed { index, entry ->
                        val barHeight = (entry.value / maxValue) * maxHeight
                        val left = (index * (barWidth + spaceBetween)) + spaceBetween / 2
                        val top = size.height - 30f - barHeight

                        // Draw bar
                        drawRoundRect(
                            color = entry.color,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    entries.forEach { entry ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entry.value.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = entry.color
                            )
                        }
                    }
                }
            }
        }
    }
}
