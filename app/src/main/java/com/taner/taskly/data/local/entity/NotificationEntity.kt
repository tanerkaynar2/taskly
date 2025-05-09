package com.taner.taskly.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["taskId"], unique = true)
        , Index("dayOfYear","year","dayOfMonth","daysOfWeek")
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfYear: Int,         // 1-365/366
    val month: Int,             // 1-12
    val year: Int,              // 2025 gibi
    val hour: Int,              // 0-23
    val minute: Int,            // 0-59
    val lastShownDayOfYear: Int?,       // Gösterildi mi?
    val lastShownYear: Int?,       // Gösterildi mi?
    val lastShownHour: Int?,       // Gösterildi mi?
    val lastShownMinute: Int?,       // Gösterildi mi?
    val taskId: Int,            // Göreve ait ID
    val taskTitle: String,
    val taskDescription: String,
    val daysOfWeek: String?=null,//1-7
    val dayOfMonth: String?=null,//1-31
)
