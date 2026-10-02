package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        UserEntity::class,
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        NotificationSettingEntity::class,
        WhatsAppConfigEntity::class,
        PushConfigEntity::class,
        NotificationLogEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun notificationConfigDao(): NotificationConfigDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edu_attend_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Users
            val users = listOf(
                UserEntity("usr_admin", "Principal Sarah Connor", "admin@school.edu", UserRole.ADMIN),
                UserEntity("usr_teacher", "Mr. David Miller", "teacher@school.edu", UserRole.TEACHER),
                UserEntity("usr_parent_1", "Robert Hendricks (Parent)", "robert@mail.com", UserRole.PARENT, "STD2025001"),
                UserEntity("usr_parent_2", "Aisha Rahman (Parent)", "aisha@mail.com", UserRole.PARENT, "STD2025002")
            )
            db.userDao().insertUsers(users)

            // Seed Classes
            val classes = listOf(
                ClassEntity("cls_10a", "Class 10-A", "10", "2025/2026"),
                ClassEntity("cls_10b", "Class 10-B", "10", "2025/2026"),
                ClassEntity("cls_11a", "Class 11-A", "11", "2025/2026"),
                ClassEntity("cls_12a", "Class 12-A", "12", "2025/2026")
            )
            db.studentDao().insertClasses(classes)

            // Seed Students
            val students = listOf(
                StudentEntity(
                    nis = "STD2025001",
                    name = "Alex Hendricks",
                    className = "Class 10-A",
                    photoUrl = "",
                    barcodeId = "QR-STD2025001",
                    parentName = "Robert Hendricks",
                    parentPhone = "+6281234567890",
                    parentFcmToken = "fcm_token_alex_2025"
                ),
                StudentEntity(
                    nis = "STD2025002",
                    name = "Siti Nurhaliza Rahman",
                    className = "Class 10-A",
                    photoUrl = "",
                    barcodeId = "QR-STD2025002",
                    parentName = "Aisha Rahman",
                    parentPhone = "+6281987654321",
                    parentFcmToken = "fcm_token_siti_2025"
                ),
                StudentEntity(
                    nis = "STD2025003",
                    name = "Michael Jordan Jr.",
                    className = "Class 10-B",
                    photoUrl = "",
                    barcodeId = "QR-STD2025003",
                    parentName = "James Jordan",
                    parentPhone = "+6281122334455",
                    parentFcmToken = "fcm_token_michael_2025"
                ),
                StudentEntity(
                    nis = "STD2025004",
                    name = "Budi Santoso",
                    className = "Class 11-A",
                    photoUrl = "",
                    barcodeId = "QR-STD2025004",
                    parentName = "Eko Santoso",
                    parentPhone = "+6281556677889",
                    parentFcmToken = "fcm_token_budi_2025"
                ),
                StudentEntity(
                    nis = "STD2025005",
                    name = "Diana Prince",
                    className = "Class 12-A",
                    photoUrl = "",
                    barcodeId = "QR-STD2025005",
                    parentName = "Hippolyta Prince",
                    parentPhone = "+6281778899001",
                    parentFcmToken = "fcm_token_diana_2025"
                )
            )
            db.studentDao().insertStudents(students)

            // Seed Default Notification Settings
            db.notificationConfigDao().saveSettings(
                NotificationSettingEntity(
                    id = 1,
                    channel = NotificationChannelType.BOTH,
                    schoolName = "Garuda International High School",
                    autoNotifyEnabled = true,
                    lateCutoffTime = "07:30",
                    messageTemplate = "Absensi Sekolah: Ananda {STUDENT_NAME} dari kelas {CLASS} telah hadir di sekolah ({SCHOOL_NAME}) pada {TIME}, {DATE}."
                )
            )

            db.notificationConfigDao().saveWhatsAppConfig(
                WhatsAppConfigEntity(
                    id = 1,
                    provider = "WhatsApp Cloud API",
                    apiEndpoint = "https://graph.facebook.com/v18.0/10060934638321/messages",
                    apiKey = "EAAGz2...DEMO_KEY",
                    phoneNumberId = "10060934638321",
                    senderNumber = "+628110000888",
                    messageTemplate = "Absensi Sekolah\nAnanda {STUDENT_NAME} dari kelas {CLASS} telah hadir di sekolah ({SCHOOL_NAME}) pada {TIME}, {DATE}.",
                    enabled = true
                )
            )

            db.notificationConfigDao().savePushConfig(
                PushConfigEntity(
                    id = 1,
                    serverKey = "FCM_SERVER_KEY_DEMO_9988",
                    projectId = "garuda-school-fcm",
                    messageTemplate = "Ananda {STUDENT_NAME} ({CLASS}) telah hadir di sekolah pada {TIME}.",
                    enabled = true
                )
            )

            // Seed Today's Initial Attendance Records for demo visualization
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            val sampleAttendance = listOf(
                AttendanceEntity(
                    studentNis = "STD2025001",
                    studentName = "Alex Hendricks",
                    className = "Class 10-A",
                    date = todayStr,
                    scanTime = "07:12:45",
                    timestamp = System.currentTimeMillis() - 3600000,
                    status = AttendanceStatus.PRESENT,
                    scannedBy = "Mr. David Miller",
                    notificationStatus = NotificationDeliveryStatus.SENT,
                    notificationChannel = NotificationChannelType.BOTH,
                    notes = "Checked in via QR tag scanner at Main Gate"
                ),
                AttendanceEntity(
                    studentNis = "STD2025002",
                    studentName = "Siti Nurhaliza Rahman",
                    className = "Class 10-A",
                    date = todayStr,
                    scanTime = "07:25:10",
                    timestamp = System.currentTimeMillis() - 1800000,
                    status = AttendanceStatus.PRESENT,
                    scannedBy = "Mr. David Miller",
                    notificationStatus = NotificationDeliveryStatus.SENT,
                    notificationChannel = NotificationChannelType.BOTH,
                    notes = "Checked in via QR tag scanner"
                ),
                AttendanceEntity(
                    studentNis = "STD2025003",
                    studentName = "Michael Jordan Jr.",
                    className = "Class 10-B",
                    date = todayStr,
                    scanTime = "07:42:00",
                    timestamp = System.currentTimeMillis() - 900000,
                    status = AttendanceStatus.LATE,
                    scannedBy = "Mr. David Miller",
                    notificationStatus = NotificationDeliveryStatus.SENT,
                    notificationChannel = NotificationChannelType.WHATSAPP,
                    notes = "Late check-in recorded"
                )
            )
            db.attendanceDao().insertAttendanceList(sampleAttendance)

            // Seed Audit Logs
            db.auditLogDao().insertLog(
                AuditLogEntity(
                    actorName = "System",
                    actorRole = "SYSTEM",
                    action = "INITIALIZATION",
                    details = "EduAttend system initialized with default students and notification configuration"
                )
            )
        }
    }
}
