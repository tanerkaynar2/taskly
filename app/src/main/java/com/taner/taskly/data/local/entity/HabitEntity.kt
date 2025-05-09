package com.taner.taskly.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.taner.taskly.domain.model.HabitFrequency

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val explain: String,
    val days: String, // Haftalık (1-7) veya Aylık (1-31)

    val isActive: Boolean = true,
    val isImportant: Boolean = false,
    val frequency: String = HabitFrequency.DAILY.turkishName,

    val createdAt: Long = System.currentTimeMillis(),
    val reminderTimeStamp: Long? = null,

    val category: String? = null,
    val note: String? = null,
    val color: Int? = null,

    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val longestStreakDayOfYearAndYear: String? = null

)
