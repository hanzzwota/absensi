package com.example.ui.components

import android.Manifest
import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.StudentEntity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraBarcodeScannerView(
    students: List<StudentEntity>,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isSimulatorMode by remember { mutableStateOf(!cameraPermissionState.status.isGranted) }
    var manualInputText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSimulatorMode) "Simulator / Manual Scanner" else "Camera Barcode Scanner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FilterChip(
                selected = isSimulatorMode,
                onClick = { isSimulatorMode = !isSimulatorMode },
                label = { Text(if (isSimulatorMode) "Switch to Camera" else "Simulator Mode") },
                leadingIcon = {
                    Icon(
                        if (isSimulatorMode) Icons.Default.CameraAlt else Icons.Default.QrCodeScanner,
                        contentDescription = null
                    )
                }
            )
        }

        if (!isSimulatorMode && cameraPermissionState.status.isGranted) {
            // Live CameraX View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                CameraXPreview(onBarcodeDetected = onBarcodeScanned)

                // Overlay Scanner Frame
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                )

                Text(
                    text = "Align Barcode / QR Code inside box",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            // Permission Banner or Simulator Frame
            if (!cameraPermissionState.status.isGranted && !isSimulatorMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }

            // Interactive Simulator & Quick Tag Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Quick Barcode / QR Scanner Simulator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap any student card below to simulate instant scanner check-in:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Student Name Tag Selector List
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(students) { student ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onBarcodeScanned(student.barcodeId) },
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.name.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${student.className} • ${student.barcodeId}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Manual Barcode Input Row
                    Column {
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualInputText,
                                onValueChange = { manualInputText = it },
                                placeholder = { Text("Enter Barcode / Student NIS e.g. QR-STD2025001") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualInputText.isNotBlank()) {
                                        onBarcodeScanned(manualInputText.trim())
                                        manualInputText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Scan")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraXPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    // ImageProxy frame analyzer placeholder for barcode detection
                    imageProxy.close()
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
