package com.example.notification

import com.example.data.model.NotificationChannelType
import com.example.data.model.NotificationDeliveryStatus

data class NotificationResult(
    val success: Boolean,
    val channel: NotificationChannelType,
    val messageSent: String,
    val errorDetails: String = ""
)

interface INotificationAdapter {
    val channelType: NotificationChannelType
    suspend fun sendNotification(
        recipientPhoneOrToken: String,
        studentName: String,
        className: String,
        schoolName: String,
        date: String,
        time: String,
        customTemplate: String? = null
    ): NotificationResult
}
