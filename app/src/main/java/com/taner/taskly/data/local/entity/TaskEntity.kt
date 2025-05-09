package com.taner.taskly.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus

@Entity(tableName = "tasks",
    indices = [
        Index(value = ["repetition"]),
        Index(value = ["priority"]),
        Index(value = ["date"]),
        Index(value = ["time"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val date: Long?, // Görev tarihi
    val days: String?,
    val time: String?, // Görev saati
    val category: TaskCategory,
    val customCategoryDetail: String? = null,
    val status: TaskStatus, // Görev durumu
    val priority: Int, // Görev önceliği
    val notification: Boolean, // Bildirim alacak mı?
    val repetition: TaskRepetition?, // Görev tekrarı
    val locations: String?, // Konum
    val subTasks: String?, // Alt görevler    (...||...||...)
    val color: Int?, // Renk
    val attachments: String?, // Eklentiler  (...||...||...)
    val isCompleted: Boolean = false,
    val lastCompletedDate: String?=null,
    var lastNotificationTime: Long?=null,//timestamp
    var notificationDelayMin: Int?=null//dakika
)
