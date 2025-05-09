package com.taner.taskly.data.local.mapper

import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.domain.model.HabitCheck

fun HabitCheckEntity.toDomain(): HabitCheck {
    return HabitCheck(
        id = id,
        habitId = habitId,
        dayOfYear = dayOfYear,
        year = year,
        isChecked = isChecked,
        time = time,
        note = note
    )
}

fun HabitCheck.toEntity(): HabitCheckEntity {
    return HabitCheckEntity(
        id = id,
        habitId = habitId,
        dayOfYear = dayOfYear,
        year = year,
        isChecked = isChecked,
        time = time,
        note = note
    )
}
