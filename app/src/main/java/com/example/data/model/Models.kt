package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    TEACHER,
    PARENT
}

enum class AttendanceStatus {
    PRESENT,
    LATE,
    ABSENT,
    EXCUSED
}

enum class NotificationChannelType {
    PUSH,
    WHATSAPP,
    BOTH,
    DISABLED
}

enum class NotificationDeliveryStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    RETRY,
    DISABLED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val associatedStudentNis: String? = null // For parent accounts
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val id: String,
    val name: String, // e.g. "Class 10-A"
    val grade: String, // e.g. "10"
    val academicYear: String // e.g. "2025/2026"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val nis: String, // Student ID / NIS e.g., "STD2025001"
    val name: String,
    val className: String,
    val photoUrl: String = "",
    val barcodeId: String, // Value stored in QR/Barcode on student tag e.g., "QR-STD2025001"
    val parentName: String,
    val parentPhone: String,
    val parentFcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentNis: String,
    val studentName: String,
    val className: String,
    val date: String, // Format: YYYY-MM-DD
    val scanTime: String, // Format: HH:mm:ss
    val timestamp: Long = System.currentTimeMillis(),
    val status: AttendanceStatus,
    val scannedBy: String,
    val notificationStatus: NotificationDeliveryStatus = NotificationDeliveryStatus.PENDING,
    val notificationChannel: NotificationChannelType = NotificationChannelType.BOTH,
    val notes: String = ""
)

@Entity(tableName = "notification_settings")
data class NotificationSettingEntity(
    @PrimaryKey val id: Int = 1,
    val channel: NotificationChannelType = NotificationChannelType.BOTH,
    val schoolName: String = "Garuda High School",
    val autoNotifyEnabled: Boolean = true,
    val lateCutoffTime: String = "07:30", // Check-in after 07:30 is LATE
    val messageTemplate: String = "Absensi Sekolah: Ananda {STUDENT_NAME} dari kelas {CLASS} telah hadir di sekolah ({SCHOOL_NAME}) pada {TIME}, {DATE}."
)

@Entity(tableName = "whatsapp_config")
data class WhatsAppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val provider: String = "WhatsApp Cloud API", // "WhatsApp Cloud API", "Twilio", "Custom Gateway"
    val apiEndpoint: String = "https://graph.facebook.com/v18.0/10060934638321/messages",
    val apiKey: String = "EAAGz2...DEMO_API_KEY",
    val phoneNumberId: String = "10060934638321",
    val senderNumber: String = "+15550192834",
    val messageTemplate: String = "Absensi Sekolah\nAnanda {STUDENT_NAME} dari kelas {CLASS} telah hadir di sekolah ({SCHOOL_NAME}) pada {TIME}, {DATE}.",
    val enabled: Boolean = true
)

@Entity(tableName = "push_config")
data class PushConfigEntity(
    @PrimaryKey val id: Int = 1,
    val serverKey: String = "AAAAn2...DEMO_FCM_KEY",
    val projectId: String = "garuda-school-fcm",
    val messageTemplate: String = "Ananda {STUDENT_NAME} ({CLASS}) telah hadir di sekolah pada {TIME}.",
    val enabled: Boolean = true
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attendanceId: Long,
    val studentNis: String,
    val studentName: String,
    val parentPhone: String,
    val channel: NotificationChannelType,
    val message: String,
    val status: NotificationDeliveryStatus,
    val sentAt: Long = System.currentTimeMillis(),
    val errorDetails: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
