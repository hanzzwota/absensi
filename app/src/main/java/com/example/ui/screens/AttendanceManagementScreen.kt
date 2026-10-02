package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserEntity
import com.example.ui.components.NotificationStatusBadge
import com.example.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceManagementScreen(
    adminViewModel: AdminViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allAttendance by adminViewModel.allAttendance.collectAsState()
    val students by adminViewModel.students.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var editingAttendance by remember { mutableStateOf<AttendanceEntity?>(null) }

    val filteredRecords = allAttendance.filter { att ->
        (selectedStatusFilter == null || att.status == selectedStatusFilter) &&
                (att.studentName.contains(searchQuery, ignoreCase = true) || att.className.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val csvData = StringBuilder()
                        csvData.append("ID,NIS,Student Name,Class,Date,Time,Status,Scanned By,Notification\n")
                        allAttendance.forEach { a ->
                            csvData.append("${a.id},${a.studentNis},\"${a.studentName}\",${a.className},${a.date},${a.scanTime},${a.status},\"${a.scannedBy}\",${a.notificationStatus}\n")
                        }
                        Toast.makeText(context, "Exported ${allAttendance.size} records to CSV format", Toast.LENGTH_LONG).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showManualAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.EditNote, contentDescription = "Manual Attendance Check-in")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search & Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter by student name or class...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All") }
                )
                AttendanceStatus.values().forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Records List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecords) { att ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingAttendance = att },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = att.studentName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${att.className} • NIS: ${att.studentNis}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                NotificationStatusBadge(status = att.notificationStatus)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: ${att.date} ${att.scanTime} • Status: ${att.status}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                TextButton(onClick = { editingAttendance = att }) {
                                    Text("Change Status")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Attendance Dialog
    if (showManualAddDialog) {
        var selectedStudentNis by remember { mutableStateOf(students.firstOrNull()?.nis ?: "") }
        var status by remember { mutableStateOf(AttendanceStatus.PRESENT) }
        var notes by remember { mutableStateOf("Manual check-in by administrator") }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            title = { Text("Add Manual Attendance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Student:", style = MaterialTheme.typography.labelMedium)
                    students.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStudentNis = s.nis }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStudentNis == s.nis,
                                onClick = { selectedStudentNis = s.nis }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${s.name} (${s.className})")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AttendanceStatus.values().forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedStudentNis.isNotBlank()) {
                        adminViewModel.addManualAttendance(
                            studentNis = selectedStudentNis,
                            date = todayStr,
                            time = timeStr,
                            status = status,
                            adminName = currentUser.name,
                            notes = notes
                        )
                        showManualAddDialog = false
                    }
                }) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Attendance Status Dialog
    if (editingAttendance != null) {
        var newStatus by remember { mutableStateOf(editingAttendance!!.status) }

        AlertDialog(
            onDismissRequest = { editingAttendance = null },
            title = { Text("Edit Attendance Status") },
            text = {
                Column {
                    Text("Student: ${editingAttendance!!.studentName}")
                    Text("Date: ${editingAttendance!!.date} ${editingAttendance!!.scanTime}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select New Status:")
                    AttendanceStatus.values().forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newStatus = st }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = newStatus == st, onClick = { newStatus = st })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminViewModel.updateAttendanceStatus(editingAttendance!!, newStatus, currentUser.name)
                    editingAttendance = null
                }) {
                    Text("Update Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAttendance = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
