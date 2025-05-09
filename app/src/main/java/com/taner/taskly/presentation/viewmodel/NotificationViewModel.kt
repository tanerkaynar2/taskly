package com.taner.taskly.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taner.taskly.data.local.entity.NotificationEntity
import com.taner.taskly.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {



    // Bildirim ekle
    fun addNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.insertNotification(notification)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }

    // Bildirim güncelle
    fun updateNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.updateNotification(notification)
        }
    }

    // Bildirimleri yükle
    fun loadUpcomingNotifications() {
        viewModelScope.launch {
        }
    }

    // Görev ID'sine göre bildirimleri yükle
    fun loadNotificationsByTaskId(taskId: Int) {
        viewModelScope.launch {
        }
    }

    // Bildirim sil
    fun deleteNotificationById(id: Int) {
        viewModelScope.launch {
            notificationRepository.deleteNotificationById(id)
        }
    }

    // Bildirim sil
    fun deleteNotificationsByTaskId(taskId: Int) {
        viewModelScope.launch {
            notificationRepository.deleteNotificationsByTaskId(taskId)
        }
    }

    // Bildirim sil
    fun deleteOldShownNotifications(dayOfYear: Int, year: Int) {
        viewModelScope.launch {
            notificationRepository.deleteOldShownNotifications(dayOfYear, year)
        }
    }



    // Bildirim getir (ID'ye göre)
    fun getTodayNotifications(dayOfWeek: Int, dayOfMonth: Int,dayOfYear: Int, year: Int): StateFlow<List<NotificationEntity>?> {
        val result = MutableStateFlow<List<NotificationEntity>?>(null)
        viewModelScope.launch {
            result.value = notificationRepository.getTodayNotifications(dayOfWeek, dayOfMonth, dayOfYear, year)
        }
        return result
    }



    // Bildirim getir (ID'ye göre)
    fun getNotificationById(id: Int): StateFlow<NotificationEntity?> {
        val result = MutableStateFlow<NotificationEntity?>(null)
        viewModelScope.launch {
            result.value = notificationRepository.getNotificationById(id)
        }
        return result
    }

    // Bildirim getir (ID'ye göre)
    fun getNotificationByTaskId(taskId: Int): StateFlow<NotificationEntity?> {
        val result = MutableStateFlow<NotificationEntity?>(null)
        viewModelScope.launch {
            result.value = notificationRepository.getNotificationByTaskId(taskId)
        }
        return result
    }
}
