package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserEntity
import com.example.ui.components.NotificationStatusBadge
import com.example.ui.components.StudentIdCardView
import com.example.ui.viewmodel.ParentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    parentViewModel: ParentViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    val childStudent by parentViewModel.childStudent.collectAsState()
    val history by parentViewModel.childAttendanceHistory.collectAsState()
    val notificationLogs by parentViewModel.childNotificationLogs.collectAsState()

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayRecord = history.find { it.date == todayStr }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Parent Attendance Portal", fontWeight = FontWeight.Bold)
                        Text("Guardian: ${currentUser.name}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student ID Card Overview
            if (childStudent != null) {
                item {
                    Text(
                        text = "CHILD INFORMATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StudentIdCardView(student = childStudent!!)
                }
            }

            // Today's Status Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (todayRecord?.status) {
                            AttendanceStatus.PRESENT -> Color(0xFFDCFCE7)
                            AttendanceStatus.LATE -> Color(0xFFFEF3C7)
                            AttendanceStatus.ABSENT -> Color(0xFFFEE2E2)
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TODAY'S ATTENDANCE STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (todayRecord != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (todayRecord.status == AttendanceStatus.PRESENT) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (todayRecord.status == AttendanceStatus.PRESENT) Color(0xFF15803D) else Color(0xFFB45309),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = todayRecord.status.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (todayRecord.status == AttendanceStatus.PRESENT) Color(0xFF15803D) else Color(0xFFB45309)
                                    )
                                    Text(
                                        text = "Checked in at ${todayRecord.scanTime} on ${todayRecord.date}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "NOT CHECKED IN YET",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Waiting for school scan...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Recent Notifications Received
            item {
                Text(
                    text = "REAL-TIME PARENT NOTIFICATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (notificationLogs.isEmpty()) {
                item {
                    Text("No notification logs yet.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(notificationLogs.take(5)) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${log.channel} Notification", fontWeight = FontWeight.Bold)
                                }
                                NotificationStatusBadge(status = log.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Attendance Timeline History
            item {
                Text(
                    text = "ATTENDANCE TIMELINE HISTORY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(history) { att ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Date: ${att.date}", fontWeight = FontWeight.Bold)
                            Text("Time: ${att.scanTime} • Scanned by: ${att.scannedBy}", style = MaterialTheme.typography.bodySmall)
                        }
                        Badge(
                            containerColor = if (att.status == AttendanceStatus.PRESENT) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                att.status.name,
                                color = if (att.status == AttendanceStatus.PRESENT) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
