package com.taner.taskly.data.repository


import com.taner.taskly.data.local.dao.NotificationDao
import com.taner.taskly.data.local.entity.NotificationEntity

class NotificationRepository(private val notificationDao: NotificationDao) {

    // Bildirim ekle
    suspend fun insertNotification(notification: NotificationEntity) {
        val existingNotification = notificationDao.getNotificationsByTaskId(notification.taskId)
        if (existingNotification.isEmpty()) {
            notificationDao.insertNotification(notification)
        }
    }

    // Bildirim güncelle
    suspend fun updateNotification(notification: NotificationEntity) {
        notificationDao.updateNotification(notification)
    }

    // ID'ye göre bildirim getir
    suspend fun getNotificationById(id: Int): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }

    // ID'ye göre bildirim getir
    suspend fun getTodayNotifications(dayOfWeek: Int, dayOfMonth: Int,dayOfYear: Int, year: Int): List<NotificationEntity>? {
        return notificationDao.getTodayNotifications(dayOfWeek, dayOfMonth, dayOfYear, year)
    }

    // ID'ye göre bildirim getir
    suspend fun getNotificationByTaskId(taskId: Int): NotificationEntity? {
        return notificationDao.getNotificationByTaskId(taskId)
    }

    suspend fun deleteOldShownNotifications(dayOfYear: Int, year: Int) {
        notificationDao.deleteOldShownNotifications(dayOfYear, year)
    }

    // Görev ID'sine göre bildirimleri getir
    suspend fun getNotificationsByTaskId(taskId: Int): List<NotificationEntity> {
        return notificationDao.getNotificationsByTaskId(taskId)
    }

    // Yaklaşan bildirimleri getir
    suspend fun getUpcomingNotifications(): List<NotificationEntity> {
        return notificationDao.getUpcomingNotifications()
    }
    // Yaklaşan bildirimleri getir
    suspend fun deleteAllNotifications() {
        return notificationDao.deleteAllNotifications()
    }

    // ID'ye göre bildirim sil
    suspend fun deleteNotificationById(id: Int) {
        notificationDao.deleteNotificationById(id)
    }

    // ID'ye göre bildirim sil
    suspend fun deleteNotificationsByTaskId(taskId: Int) {
        notificationDao.deleteNotificationsByTaskId(taskId)
    }
}
