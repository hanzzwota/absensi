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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity

@Composable
fun StudentIdCardView(
    student: StudentEntity,
    schoolName: String = "Garuda International High School",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = schoolName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "STUDENT IDENTITY CARD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = student.className,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student Photo / Avatar & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "NIS: ${student.nis}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Parent: ${student.parentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual QR Code & Barcode Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR Code Representation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    QrCodeCanvas(data = student.barcodeId, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan QR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Barcode Representation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Barcode1DCanvas(data = student.barcodeId, modifier = Modifier.width(120.dp).height(50.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = student.barcodeId,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun QrCodeCanvas(data: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = size.width
        val moduleCount = 11
        val moduleSize = size / moduleCount
        val hash = data.hashCode()

        // Background white
        drawRect(Color.White)

        // Draw Finder Patterns (Corners)
        fun drawFinder(x: Float, y: Float) {
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(x, y),
                size = Size(moduleSize * 3, moduleSize * 3),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x + moduleSize * 0.5f, y + moduleSize * 0.5f),
                size = Size(moduleSize * 2, moduleSize * 2),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(x + moduleSize, y + moduleSize),
                size = Size(moduleSize, moduleSize),
                cornerRadius = CornerRadius(1f, 1f)
            )
        }

        drawFinder(0f, 0f)
        drawFinder(size - moduleSize * 3, 0f)
        drawFinder(0f, size - moduleSize * 3)

        // Draw pseudo-random data modules based on data hash
        for (row in 0 until moduleCount) {
            for (col in 0 until moduleCount) {
                // Skip finder pattern zones
                val inTopLeft = row < 4 && col < 4
                val inTopRight = row < 4 && col >= moduleCount - 4
                val inBottomLeft = row >= moduleCount - 4 && col < 4
                if (!inTopLeft && !inTopRight && !inBottomLeft) {
                    val bit = ((hash xor (row * 31 + col)) and 1) == 0
                    if (bit) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(col * moduleSize, row * moduleSize),
                            size = Size(moduleSize * 0.9f, moduleSize * 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Barcode1DCanvas(data: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val hash = data.hashCode()

        drawRect(Color.White)

        val totalBars = 30
        val barWidth = width / totalBars

        for (i in 0 until totalBars) {
            val isBar = ((hash xor (i * 17)) and 1) == 0 || i == 0 || i == totalBars - 1 || i == totalBars / 2
            if (isBar) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(i * barWidth, 0f),
                    size = Size(barWidth * 0.7f, height)
                )
            }
        }
    }
}
