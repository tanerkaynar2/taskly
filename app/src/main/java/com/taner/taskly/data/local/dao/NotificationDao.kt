package com.taner.taskly.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.taner.taskly.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Int): NotificationEntity?

    @Query(
        """
    SELECT * FROM notifications 
    WHERE 
        (daysOfWeek IS NULL AND dayOfMonth IS NULL AND year = :year AND dayOfYear = :dayOfYear)
        OR (daysOfWeek IS NOT NULL AND daysOfWeek LIKE '%' || :dayOfWeek || '%')
        OR (dayOfMonth IS NOT NULL AND dayOfMonth LIKE '%' || :dayOfMonth || '%')
""")
    suspend fun getTodayNotifications(dayOfWeek: Int, dayOfMonth: Int,dayOfYear: Int, year: Int): List<NotificationEntity>?

    @Query("SELECT * FROM notifications WHERE taskId = :taskId")
    suspend fun getNotificationByTaskId(taskId: Int): NotificationEntity?

    @Query("DELETE FROM notifications WHERE daysOfWeek is null AND dayOfMonth is null AND (year<:year or (year = :year and dayOfYear<:dayOfYear))")
    suspend fun deleteOldShownNotifications(dayOfYear: Int, year: Int)

    @Query("SELECT * FROM notifications WHERE taskId = :taskId")
    suspend fun getNotificationsByTaskId(taskId: Int): List<NotificationEntity>


    @Query("SELECT * FROM notifications ORDER BY dayOfYear ASC, hour ASC, minute ASC")
    suspend fun getUpcomingNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getSize(): Int

    @Query("DELETE FROM notifications WHERE taskId = :taskId")
    suspend fun deleteNotificationsByTaskId(taskId: Int)
}
