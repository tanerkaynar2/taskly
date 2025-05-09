package com.taner.taskly.domain.model

enum class HabitFrequency(val turkishName: String) {
    DAILY("Her Gün"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık");

    override fun toString(): String {
        return turkishName//super.toString()
    }
}


