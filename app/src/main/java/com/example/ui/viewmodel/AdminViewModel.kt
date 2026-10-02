package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.AuditLogRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminViewModel(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val notificationRepository: NotificationRepository,
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    val students: StateFlow<List<StudentEntity>> = studentRepository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<ClassEntity>> = studentRepository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceEntity>> = attendanceRepository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<List<AttendanceEntity>> = attendanceRepository.getTodayAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationSettings: StateFlow<NotificationSettingEntity?> = notificationRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val whatsAppConfig: StateFlow<WhatsAppConfigEntity?> = notificationRepository.whatsAppConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pushConfig: StateFlow<PushConfigEntity?> = notificationRepository.pushConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = notificationRepository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = auditLogRepository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student CRUD
    fun addStudent(student: StudentEntity, adminName: String) {
        viewModelScope.launch {
            studentRepository.insertStudent(student)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "ADD_STUDENT",
                details = "Added student ${student.name} (${student.nis}) in ${student.className}"
            )
        }
    }

    fun updateStudent(student: StudentEntity, adminName: String) {
        viewModelScope.launch {
            studentRepository.updateStudent(student)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "UPDATE_STUDENT",
                details = "Updated details for student ${student.name} (${student.nis})"
            )
        }
    }

    fun deleteStudent(nis: String, adminName: String) {
        viewModelScope.launch {
            studentRepository.deleteStudent(nis)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "DELETE_STUDENT",
                details = "Deleted student with NIS $nis"
            )
        }
    }

    // Class Management
    fun addClass(classEntity: ClassEntity, adminName: String) {
        viewModelScope.launch {
            studentRepository.insertClass(classEntity)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "ADD_CLASS",
                details = "Created class ${classEntity.name}"
            )
        }
    }

    // Attendance Corrections & Manual Check-in
    fun addManualAttendance(
        studentNis: String,
        date: String,
        time: String,
        status: AttendanceStatus,
        adminName: String,
        notes: String
    ) {
        viewModelScope.launch {
            attendanceRepository.insertManualAttendance(studentNis, date, time, status, adminName, notes)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "MANUAL_ATTENDANCE",
                details = "Manually checked in student NIS $studentNis as $status on $date at $time"
            )
        }
    }

    fun updateAttendanceStatus(attendance: AttendanceEntity, newStatus: AttendanceStatus, adminName: String) {
        viewModelScope.launch {
            attendanceRepository.updateAttendance(attendance.copy(status = newStatus))
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "EDIT_ATTENDANCE",
                details = "Updated attendance ID ${attendance.id} status to $newStatus"
            )
        }
    }

    // Notification Settings
    fun updateNotificationChannel(channel: NotificationChannelType, adminName: String) {
        viewModelScope.launch {
            val current = notificationSettings.value ?: NotificationSettingEntity()
            val updated = current.copy(channel = channel)
            notificationRepository.saveSettings(updated)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "UPDATE_NOTIFICATION_CHANNEL",
                details = "Changed notification channel setting to $channel"
            )
        }
    }

    fun saveWhatsAppConfig(config: WhatsAppConfigEntity, adminName: String) {
        viewModelScope.launch {
            notificationRepository.saveWhatsAppConfig(config)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "UPDATE_WHATSAPP_CONFIG",
                details = "Updated WhatsApp configuration (Provider: ${config.provider}, Enabled: ${config.enabled})"
            )
        }
    }

    fun savePushConfig(config: PushConfigEntity, adminName: String) {
        viewModelScope.launch {
            notificationRepository.savePushConfig(config)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "UPDATE_PUSH_CONFIG",
                details = "Updated Push notification configuration (Enabled: ${config.enabled})"
            )
        }
    }

    fun retryFailedNotification(log: NotificationLogEntity, adminName: String) {
        viewModelScope.launch {
            val success = notificationRepository.retryNotification(log)
            auditLogRepository.logAction(
                actorName = adminName,
                actorRole = "ADMIN",
                action = "RETRY_NOTIFICATION",
                details = "Retried notification ID ${log.id} for ${log.studentName}. Result success: $success"
            )
        }
    }
}
