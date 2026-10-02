package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import com.example.ui.components.CameraBarcodeScannerView
import com.example.ui.components.NotificationStatusBadge
import com.example.ui.components.StudentIdCardView
import com.example.ui.viewmodel.ScannerUiState
import com.example.ui.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    scannerViewModel: ScannerViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    val students by scannerViewModel.allStudents.collectAsState()
    val scanState by scannerViewModel.scanState.collectAsState()
    val lastStudent by scannerViewModel.lastScannedStudent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Staff: ${currentUser.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { scannerViewModel.resetScanState() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Scanner")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // Live Status Banner
            AnimatedContent(
                targetState = scanState,
                label = "ScanStatusAnimation"
            ) { state ->
                when (state) {
                    is ScannerUiState.Idle -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "READY TO SCAN",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Scan student QR tag or NIS code to check in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    is ScannerUiState.Success -> {
                        val att = state.result.attendance
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "SUCCESS: ${att.studentName}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFF15803D)
                                        )
                                        NotificationStatusBadge(status = state.result.notificationStatus)
                                    }
                                    Text(
                                        text = "Class: ${att.className} • Time: ${att.scanTime} • Status: ${att.status}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "Parent notification automatically dispatched to ${state.result.student.parentPhone}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF166534).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    is ScannerUiState.Duplicate -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ALREADY CHECKED IN TODAY",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = "${state.result.student.name} was already checked in at ${state.result.existingRecord.scanTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }

                    is ScannerUiState.NotFound -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFB91C1C)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "INVALID / UNKNOWN BARCODE",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFFB91C1C)
                                    )
                                    Text(
                                        text = "No student found matching code '${state.code}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            // Scanner Camera Frame & Simulator Box
            Box(modifier = Modifier.weight(1f)) {
                CameraBarcodeScannerView(
                    students = students,
                    onBarcodeScanned = { code ->
                        scannerViewModel.onBarcodeScanned(code, currentUser.name)
                    }
                )
            }

            // Last Scanned Student Summary
            if (lastStudent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LAST SCANNED STUDENT CARD",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                StudentIdCardView(student = lastStudent!!)
            }
        }
    }
}
