package com.example.notification

import com.example.data.local.NotificationConfigDao
import com.example.data.model.NotificationChannelType
import com.example.data.model.NotificationDeliveryStatus
import com.example.data.model.NotificationLogEntity
import com.example.data.model.StudentEntity

class NotificationDispatcher(
    private val notificationDao: NotificationConfigDao
) {
    private val whatsAppAdapter = WhatsAppNotificationAdapter {
        notificationDao.getWhatsAppConfigSync()
    }

    private val pushAdapter = PushNotificationAdapter {
        notificationDao.getPushConfigSync()
    }

    suspend fun dispatchAttendanceNotification(
        attendanceId: Long,
        student: StudentEntity,
        date: String,
        time: String
    ): NotificationDeliveryStatus {
        val settings = notificationDao.getSettingsSync() ?: return NotificationDeliveryStatus.DISABLED
        if (!settings.autoNotifyEnabled || settings.channel == NotificationChannelType.DISABLED) {
            return NotificationDeliveryStatus.DISABLED
        }

        var overallSuccess = false
        val errors = mutableListOf<String>()

        if (settings.channel == NotificationChannelType.WHATSAPP || settings.channel == NotificationChannelType.BOTH) {
            val result = whatsAppAdapter.sendNotification(
                recipientPhoneOrToken = student.parentPhone,
                studentName = student.name,
                className = student.className,
                schoolName = settings.schoolName,
                date = date,
                time = time,
                customTemplate = settings.messageTemplate
            )

            val logStatus = if (result.success) NotificationDeliveryStatus.SENT else NotificationDeliveryStatus.FAILED
            notificationDao.insertLog(
                NotificationLogEntity(
                    attendanceId = attendanceId,
                    studentNis = student.nis,
                    studentName = student.name,
                    parentPhone = student.parentPhone,
                    channel = NotificationChannelType.WHATSAPP,
                    message = result.messageSent,
                    status = logStatus,
                    errorDetails = result.errorDetails
                )
            )

            if (result.success) overallSuccess = true
            else if (result.errorDetails.isNotEmpty()) errors.add("WhatsApp: ${result.errorDetails}")
        }

        if (settings.channel == NotificationChannelType.PUSH || settings.channel == NotificationChannelType.BOTH) {
            val token = student.parentFcmToken.ifEmpty { "fcm_token_default" }
            val result = pushAdapter.sendNotification(
                recipientPhoneOrToken = token,
                studentName = student.name,
                className = student.className,
                schoolName = settings.schoolName,
                date = date,
                time = time,
                customTemplate = settings.messageTemplate
            )

            val logStatus = if (result.success) NotificationDeliveryStatus.SENT else NotificationDeliveryStatus.FAILED
            notificationDao.insertLog(
                NotificationLogEntity(
                    attendanceId = attendanceId,
                    studentNis = student.nis,
                    studentName = student.name,
                    parentPhone = student.parentPhone,
                    channel = NotificationChannelType.PUSH,
                    message = result.messageSent,
                    status = logStatus,
                    errorDetails = result.errorDetails
                )
            )

            if (result.success) overallSuccess = true
            else if (result.errorDetails.isNotEmpty()) errors.add("Push: ${result.errorDetails}")
        }

        return if (overallSuccess) NotificationDeliveryStatus.SENT else NotificationDeliveryStatus.FAILED
    }
}
