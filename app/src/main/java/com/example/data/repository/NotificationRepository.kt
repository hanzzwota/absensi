package com.example.data.repository

import com.example.data.local.AuditLogDao
import com.example.data.local.NotificationConfigDao
import com.example.data.local.StudentDao
import com.example.data.model.*
import com.example.notification.NotificationDispatcher
import com.example.notification.PushNotificationAdapter
import com.example.notification.WhatsAppNotificationAdapter
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val notificationDao: NotificationConfigDao,
    private val studentDao: StudentDao
) {
    val settings: Flow<NotificationSettingEntity?> = notificationDao.getSettings()
    val whatsAppConfig: Flow<WhatsAppConfigEntity?> = notificationDao.getWhatsAppConfig()
    val pushConfig: Flow<PushConfigEntity?> = notificationDao.getPushConfig()
    val allLogs: Flow<List<NotificationLogEntity>> = notificationDao.getAllLogs()

    fun getLogsByStudent(studentNis: String): Flow<List<NotificationLogEntity>> =
        notificationDao.getLogsByStudent(studentNis)

    suspend fun saveSettings(settings: NotificationSettingEntity) =
        notificationDao.saveSettings(settings)

    suspend fun saveWhatsAppConfig(config: WhatsAppConfigEntity) =
        notificationDao.saveWhatsAppConfig(config)

    suspend fun savePushConfig(config: PushConfigEntity) =
        notificationDao.savePushConfig(config)

    suspend fun testWhatsAppConnection(testPhone: String, message: String): Boolean {
        val adapter = WhatsAppNotificationAdapter { notificationDao.getWhatsAppConfigSync() }
        val settings = notificationDao.getSettingsSync()
        val res = adapter.sendNotification(
            recipientPhoneOrToken = testPhone,
            studentName = "Test Student",
            className = "Test Class",
            schoolName = settings?.schoolName ?: "Garuda High School",
            date = "2026-10-02",
            time = "07:30:00",
            customTemplate = message
        )
        return res.success
    }

    suspend fun testPushConnection(testToken: String, message: String): Boolean {
        val adapter = PushNotificationAdapter { notificationDao.getPushConfigSync() }
        val settings = notificationDao.getSettingsSync()
        val res = adapter.sendNotification(
            recipientPhoneOrToken = testToken,
            studentName = "Test Student",
            className = "Test Class",
            schoolName = settings?.schoolName ?: "Garuda High School",
            date = "2026-10-02",
            time = "07:30:00",
            customTemplate = message
        )
        return res.success
    }

    suspend fun retryNotification(log: NotificationLogEntity): Boolean {
        val student = studentDao.getStudentByNis(log.studentNis) ?: return false
        val settings = notificationDao.getSettingsSync() ?: return false

        val dispatcher = NotificationDispatcher(notificationDao)
        val status = dispatcher.dispatchAttendanceNotification(
            attendanceId = log.attendanceId,
            student = student,
            date = "Today",
            time = "Check-in"
        )

        notificationDao.updateLog(
            log.copy(
                status = status,
                sentAt = System.currentTimeMillis(),
                errorDetails = if (status == NotificationDeliveryStatus.SENT) "" else "Retry failed"
            )
        )
        return status == NotificationDeliveryStatus.SENT
    }
}

class AuditLogRepository(private val auditLogDao: AuditLogDao) {
    val allLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    suspend fun logAction(actorName: String, actorRole: String, action: String, details: String) {
        auditLogDao.insertLog(
            AuditLogEntity(
                actorName = actorName,
                actorRole = actorRole,
                action = action,
                details = details
            )
        )
    }
}
