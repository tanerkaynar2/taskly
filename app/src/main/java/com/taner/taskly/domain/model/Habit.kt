package com.taner.taskly.domain.model

data class Habit(
    val id: Int = 0,
    val name: String,
    val explain: String,
    val days: List<Int>, // Hangi günler yapılacağı (örn: haftlaıksa 1-7 arası aylıksa 1-31 arası)
    val isImportant: Boolean = false,// Devre dışı bırakmak için
    var isActive: Boolean = true,// Devre dışı bırakmak için
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTimeStamp: Long?=null,
    val category: String?=null,
    var note: String?=null,
    val color: Int? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val longestStreakDayOfYear: Int? = null,
    val longestStreakYear: Int? = null
)
