package com.example.data.local

import androidx.room.*
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.NotificationDeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY id DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY id DESC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentNis = :nis ORDER BY date DESC, scanTime DESC")
    fun getAttendanceByStudent(nis: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentNis = :nis AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentOnDate(nis: String, date: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("UPDATE attendance SET notificationStatus = :status WHERE id = :id")
    suspend fun updateNotificationStatus(id: Long, status: NotificationDeliveryStatus)

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = :status")
    fun getCountByStatusOnDate(date: String, status: AttendanceStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date")
    fun getTotalPresentOnDate(date: String): Flow<Int>

    @Query("SELECT * FROM attendance WHERE date = :date AND className = :className ORDER BY studentName ASC")
    fun getAttendanceByClassAndDate(className: String, date: String): Flow<List<AttendanceEntity>>

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)
}
