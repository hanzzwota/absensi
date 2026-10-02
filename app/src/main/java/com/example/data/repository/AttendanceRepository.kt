package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.NotificationConfigDao
import com.example.data.local.StudentDao
import com.example.data.model.*
import com.example.notification.NotificationDispatcher
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

sealed class ScanResult {
    data class Success(
        val attendance: AttendanceEntity,
        val student: StudentEntity,
        val notificationStatus: NotificationDeliveryStatus,
        val isDuplicate: Boolean = false
    ) : ScanResult()

    data class AlreadyScanned(
        val existingRecord: AttendanceEntity,
        val student: StudentEntity
    ) : ScanResult()

    data class NotFound(val scannedCode: String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val studentDao: StudentDao,
    private val notificationConfigDao: NotificationConfigDao,
    private val notificationDispatcher: NotificationDispatcher
) {

    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()

    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDate(date)

    fun getAttendanceByStudent(nis: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByStudent(nis)

    fun getTodayAttendance(): Flow<List<AttendanceEntity>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return attendanceDao.getAttendanceByDate(today)
    }

    suspend fun processBarcodeScan(barcodeId: String, scannedBy: String): ScanResult {
        val cleanCode = barcodeId.trim()
        val student = studentDao.getStudentByBarcode(cleanCode)
            ?: studentDao.getStudentByNis(cleanCode)
            ?: return ScanResult.NotFound(cleanCode)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val now = Date()
        val todayStr = dateFormat.format(now)
        val timeStr = timeFormat.format(now)

        // Prevent duplicate attendance scan for today
        val existing = attendanceDao.getAttendanceForStudentOnDate(student.nis, todayStr)
        if (existing != null) {
            return ScanResult.AlreadyScanned(existing, student)
        }

        // Determine if Present or Late based on settings cutoff time
        val settings = notificationConfigDao.getSettingsSync()
        val cutoff = settings?.lateCutoffTime ?: "07:30"
        val isLate = timeStr > cutoff
        val status = if (isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT

        val newRecord = AttendanceEntity(
            studentNis = student.nis,
            studentName = student.name,
            className = student.className,
            date = todayStr,
            scanTime = timeStr,
            timestamp = System.currentTimeMillis(),
            status = status,
            scannedBy = scannedBy,
            notificationStatus = NotificationDeliveryStatus.PENDING,
            notificationChannel = settings?.channel ?: NotificationChannelType.BOTH
        )

        val id = attendanceDao.insertAttendance(newRecord)
        val savedRecord = newRecord.copy(id = id)

        // Automatically trigger notification
        val notifyStatus = notificationDispatcher.dispatchAttendanceNotification(
            attendanceId = id,
            student = student,
            date = todayStr,
            time = timeStr
        )

        attendanceDao.updateNotificationStatus(id, notifyStatus)

        return ScanResult.Success(
            attendance = savedRecord.copy(notificationStatus = notifyStatus),
            student = student,
            notificationStatus = notifyStatus
        )
    }

    suspend fun insertManualAttendance(
        studentNis: String,
        date: String,
        time: String,
        status: AttendanceStatus,
        scannedBy: String,
        notes: String
    ): Boolean {
        val student = studentDao.getStudentByNis(studentNis) ?: return false
        val record = AttendanceEntity(
            studentNis = student.nis,
            studentName = student.name,
            className = student.className,
            date = date,
            scanTime = time,
            timestamp = System.currentTimeMillis(),
            status = status,
            scannedBy = scannedBy,
            notificationStatus = NotificationDeliveryStatus.PENDING,
            notes = notes
        )
        val id = attendanceDao.insertAttendance(record)

        val notifyStatus = notificationDispatcher.dispatchAttendanceNotification(
            attendanceId = id,
            student = student,
            date = date,
            time = time
        )
        attendanceDao.updateNotificationStatus(id, notifyStatus)
        return true
    }

    suspend fun updateAttendance(attendance: AttendanceEntity) {
        attendanceDao.updateAttendance(attendance)
    }

    suspend fun deleteAttendance(id: Long) {
        attendanceDao.deleteAttendanceById(id)
    }
}
