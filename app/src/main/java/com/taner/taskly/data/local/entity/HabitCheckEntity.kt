package com.taner.taskly.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habit_checks",
    indices = [Index(value = ["habitId","dayOfYear","year"])]
)
data class HabitCheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: Int,
    val dayOfYear: Int,
    val year: Int,
    val isChecked: Boolean,
    val time: Long,
    val note: String = ""
)
