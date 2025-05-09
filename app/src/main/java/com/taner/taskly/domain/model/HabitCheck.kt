package com.taner.taskly.domain.model

data class HabitCheck(
    var id: Int = 0,
    val habitId: Int,
    val dayOfYear: Int,
    val year: Int,
    val isChecked: Boolean,
    val time:Long,
    val note: String = ""
)