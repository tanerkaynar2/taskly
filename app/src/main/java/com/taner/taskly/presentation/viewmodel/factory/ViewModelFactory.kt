package com.taner.taskly.presentation.viewmodel.factory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taner.taskly.data.repository.HabitCheckRepository
import com.taner.taskly.data.repository.TaskRepository
import com.taner.taskly.data.repository.HabitRepository
import com.taner.taskly.data.repository.NotificationRepository
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.NotificationViewModel

class ViewModelFactory(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val habitCheckRepository: HabitCheckRepository,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {



    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> TaskViewModel(taskRepository) as T
            modelClass.isAssignableFrom(HabitViewModel::class.java) -> HabitViewModel(habitRepository, habitCheckRepository) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(notificationRepository) as T
            modelClass.isAssignableFrom(HabitCheckViewModel::class.java) -> HabitCheckViewModel(habitCheckRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
