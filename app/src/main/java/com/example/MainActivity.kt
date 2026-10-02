package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.AuditLogRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.StudentRepository
import com.example.notification.NotificationDispatcher
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ParentViewModel
import com.example.ui.viewmodel.ScannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val studentRepository = StudentRepository(database.studentDao())
        val notificationRepository = NotificationRepository(database.notificationConfigDao(), database.studentDao())
        val auditLogRepository = AuditLogRepository(database.auditLogDao())
        val notificationDispatcher = NotificationDispatcher(database.notificationConfigDao())

        val attendanceRepository = AttendanceRepository(
            attendanceDao = database.attendanceDao(),
            studentDao = database.studentDao(),
            notificationConfigDao = database.notificationConfigDao(),
            notificationDispatcher = notificationDispatcher
        )

        val mainViewModel = MainViewModel(auditLogRepository)
        val scannerViewModel = ScannerViewModel(attendanceRepository, studentRepository, auditLogRepository)
        val adminViewModel = AdminViewModel(studentRepository, attendanceRepository, notificationRepository, auditLogRepository)
        val parentViewModel = ParentViewModel(studentRepository, attendanceRepository, notificationRepository)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        mainViewModel = mainViewModel,
                        scannerViewModel = scannerViewModel,
                        adminViewModel = adminViewModel,
                        parentViewModel = parentViewModel
                    )
                }
            }
        }
    }
}
