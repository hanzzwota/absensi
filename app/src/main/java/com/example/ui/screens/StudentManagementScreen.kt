package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import com.example.ui.components.StudentIdCardView
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    adminViewModel: AdminViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    val students by adminViewModel.students.collectAsState()
    val classes by adminViewModel.classes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("ALL") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var viewingCardStudent by remember { mutableStateOf<StudentEntity?>(null) }

    val filteredStudents = students.filter { s ->
        (selectedClassFilter == "ALL" || s.className == selectedClassFilter) &&
                (s.name.contains(searchQuery, ignoreCase = true) || s.nis.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Directory", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name or NIS...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            ScrollableTabRow(
                selectedTabIndex = if (selectedClassFilter == "ALL") 0 else classes.indexOfFirst { it.name == selectedClassFilter } + 1,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedClassFilter == "ALL",
                    onClick = { selectedClassFilter = "ALL" },
                    text = { Text("All Classes") }
                )
                classes.forEach { cls ->
                    Tab(
                        selected = selectedClassFilter == cls.name,
                        onClick = { selectedClassFilter = cls.name },
                        text = { Text(cls.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStudents) { student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewingCardStudent = student },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "NIS: ${student.nis} • Class: ${student.className}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Parent: ${student.parentName} (${student.parentPhone})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(onClick = { viewingCardStudent = student }) {
                                    Icon(Icons.Default.QrCode, contentDescription = "View ID Card / QR")
                                }
                                IconButton(onClick = { editingStudent = student }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Student")
                                }
                                IconButton(onClick = { adminViewModel.deleteStudent(student.nis, currentUser.name) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddDialog || editingStudent != null) {
        val isEditing = editingStudent != null
        val target = editingStudent

        var nis by remember { mutableStateOf(target?.nis ?: "STD202500${students.size + 1}") }
        var name by remember { mutableStateOf(target?.name ?: "") }
        var className by remember { mutableStateOf(target?.className ?: "Class 10-A") }
        var barcodeId by remember { mutableStateOf(target?.barcodeId ?: "QR-STD202500${students.size + 1}") }
        var parentName by remember { mutableStateOf(target?.parentName ?: "") }
        var parentPhone by remember { mutableStateOf(target?.parentPhone ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingStudent = null
            },
            title = { Text(if (isEditing) "Edit Student Details" else "Add New Student") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nis,
                        onValueChange = { nis = it },
                        label = { Text("NIS / Student ID") },
                        enabled = !isEditing
                    )
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") })
                    OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name") })
                    OutlinedTextField(value = barcodeId, onValueChange = { barcodeId = it }, label = { Text("Barcode/QR ID") })
                    OutlinedTextField(value = parentName, onValueChange = { parentName = it }, label = { Text("Parent/Guardian Name") })
                    OutlinedTextField(value = parentPhone, onValueChange = { parentPhone = it }, label = { Text("Parent Phone Number") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && nis.isNotBlank()) {
                            val newStudent = StudentEntity(
                                nis = nis,
                                name = name,
                                className = className,
                                barcodeId = barcodeId,
                                parentName = parentName,
                                parentPhone = parentPhone
                            )
                            if (isEditing) adminViewModel.updateStudent(newStudent, currentUser.name)
                            else adminViewModel.addStudent(newStudent, currentUser.name)

                            showAddDialog = false
                            editingStudent = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingStudent = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Viewing Student ID Card Modal with Printable QR / Barcode Tag
    if (viewingCardStudent != null) {
        Dialog(onDismissRequest = { viewingCardStudent = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StudentIdCardView(student = viewingCardStudent!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewingCardStudent = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Card View")
                    }
                }
            }
        }
    }
}
