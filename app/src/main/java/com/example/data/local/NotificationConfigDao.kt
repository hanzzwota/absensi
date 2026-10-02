package com.example.data.local

import androidx.room.*
import com.example.data.model.NotificationLogEntity
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PushConfigEntity
import com.example.data.model.WhatsAppConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationConfigDao {
    @Query("SELECT * FROM notification_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<NotificationSettingEntity?>

    @Query("SELECT * FROM notification_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): NotificationSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: NotificationSettingEntity)

    @Query("SELECT * FROM whatsapp_config WHERE id = 1 LIMIT 1")
    fun getWhatsAppConfig(): Flow<WhatsAppConfigEntity?>

    @Query("SELECT * FROM whatsapp_config WHERE id = 1 LIMIT 1")
    suspend fun getWhatsAppConfigSync(): WhatsAppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWhatsAppConfig(config: WhatsAppConfigEntity)

    @Query("SELECT * FROM push_config WHERE id = 1 LIMIT 1")
    fun getPushConfig(): Flow<PushConfigEntity?>

    @Query("SELECT * FROM push_config WHERE id = 1 LIMIT 1")
    suspend fun getPushConfigSync(): PushConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePushConfig(config: PushConfigEntity)

    // Notification Logs
    @Query("SELECT * FROM notification_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_logs WHERE studentNis = :studentNis ORDER BY id DESC")
    fun getLogsByStudent(studentNis: String): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Update
    suspend fun updateLog(log: NotificationLogEntity)
}
