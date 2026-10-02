package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.screens.*
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ParentViewModel
import com.example.ui.viewmodel.ScannerViewModel

enum class Screen(val title: String, val icon: ImageVector) {
    SCANNER("Scanner", Icons.Default.QrCodeScanner),
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    STUDENTS("Students", Icons.Default.People),
    ATTENDANCE("Attendance", Icons.Default.ListAlt),
    SETTINGS("Notification Config", Icons.Default.Settings),
    AUDIT("Audit Logs", Icons.Default.History),
    PARENT_PORTAL("Parent View", Icons.Default.FamilyRestroom)
}

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    scannerViewModel: ScannerViewModel,
    adminViewModel: AdminViewModel,
    parentViewModel: ParentViewModel
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.SCANNER) }

    if (currentUser == null) {
        LoginScreen(
            onLoginSelected = { user ->
                mainViewModel.loginAs(user)
                currentScreen = if (user.role == UserRole.PARENT) Screen.PARENT_PORTAL else Screen.SCANNER
            }
        )
    } else {
        val user = currentUser!!

        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EduAttend • ${user.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Role: ${user.role.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { mainViewModel.logout() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Role")
                        }
                    }
                }
            },
            bottomBar = {
                if (user.role != UserRole.PARENT) {
                    NavigationBar {
                        listOf(Screen.SCANNER, Screen.DASHBOARD, Screen.STUDENTS, Screen.ATTENDANCE, Screen.SETTINGS).forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when {
                    user.role == UserRole.PARENT || currentScreen == Screen.PARENT_PORTAL -> {
                        ParentDashboardScreen(
                            parentViewModel = parentViewModel,
                            currentUser = user
                        )
                    }

                    currentScreen == Screen.SCANNER -> {
                        ScannerScreen(
                            scannerViewModel = scannerViewModel,
                            currentUser = user
                        )
                    }

                    currentScreen == Screen.DASHBOARD -> {
                        AdminDashboardScreen(
                            adminViewModel = adminViewModel,
                            currentUser = user,
                            onNavigateToSection = { section ->
                                when (section) {
                                    "students" -> currentScreen = Screen.STUDENTS
                                    "attendance" -> currentScreen = Screen.ATTENDANCE
                                    "settings" -> currentScreen = Screen.SETTINGS
                                }
                            }
                        )
                    }

                    currentScreen == Screen.STUDENTS -> {
                        StudentManagementScreen(
                            adminViewModel = adminViewModel,
                            currentUser = user
                        )
                    }

                    currentScreen == Screen.ATTENDANCE -> {
                        AttendanceManagementScreen(
                            adminViewModel = adminViewModel,
                            currentUser = user
                        )
                    }

                    currentScreen == Screen.SETTINGS -> {
                        NotificationSettingsScreen(
                            adminViewModel = adminViewModel,
                            currentUser = user
                        )
                    }

                    currentScreen == Screen.AUDIT -> {
                        AuditLogsScreen(
                            adminViewModel = adminViewModel
                        )
                    }
                }
            }
        }
    }
}
