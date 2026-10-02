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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserEntity
import com.example.ui.components.AttendanceBarChart
import com.example.ui.components.BarChartEntry
import com.example.ui.components.NotificationStatusBadge
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    currentUser: UserEntity,
    onNavigateToSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val students by adminViewModel.students.collectAsState()
    val todayAttendance by adminViewModel.todayAttendance.collectAsState()

    val totalStudentsCount = students.size
    val presentCount = todayAttendance.count { it.status == AttendanceStatus.PRESENT }
    val lateCount = todayAttendance.count { it.status == AttendanceStatus.LATE }
    val absentCount = (totalStudentsCount - presentCount - lateCount).coerceAtLeast(0)
    val attendancePct = if (totalStudentsCount > 0) ((presentCount + lateCount).toFloat() / totalStudentsCount * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.Bold)
                        Text("Real-Time School Overview", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToSection("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Notification Settings")
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
            // KPI Summary Cards Grid
            item {
                Text(
                    text = "TODAY'S ATTENDANCE SUMMARY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Total",
                        value = "$totalStudentsCount",
                        icon = Icons.Default.Groups,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Present",
                        value = "$presentCount",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Late",
                        value = "$lateCount",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Rate",
                        value = "$attendancePct%",
                        icon = Icons.Default.Analytics,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Quick Management Links
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToSection("students") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage Students", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToSection("attendance") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attendance Log", fontSize = 12.sp)
                    }
                }
            }

            // Attendance Breakdown Bar Chart
            item {
                val chartEntries = listOf(
                    BarChartEntry("Present", presentCount.toFloat(), Color(0xFF15803D)),
                    BarChartEntry("Late", lateCount.toFloat(), Color(0xFFB45309)),
                    BarChartEntry("Absent", absentCount.toFloat(), Color(0xFFB91C1C))
                )
                AttendanceBarChart(
                    title = "Today's Status Breakdown",
                    entries = chartEntries
                )
            }

            // Live Attendance Feed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE ATTENDANCE FEED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { onNavigateToSection("attendance") }) {
                        Text("View All")
                    }
                }
            }

            if (todayAttendance.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No check-ins recorded yet today.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(todayAttendance.take(10)) { att ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (att.status == AttendanceStatus.PRESENT) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                        RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (att.status == AttendanceStatus.PRESENT) Icons.Default.Check else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (att.status == AttendanceStatus.PRESENT) Color(0xFF15803D) else Color(0xFFB45309)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = att.studentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${att.className} • Time: ${att.scanTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            NotificationStatusBadge(
                                status = att.notificationStatus,
                                onRetry = {
                                    // Retry callback available
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
