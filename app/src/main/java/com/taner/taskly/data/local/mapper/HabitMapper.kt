package com.taner.taskly.data.local.mapper

import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitFrequency

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        name = name,
        explain = explain,
        days = days.split("||").map { it.replace("-","").replace("<","").replace(">","").toInt() },
        isActive = isActive,
        frequency = HabitFrequency.entries.get(HabitFrequency.entries.map { it.turkishName }.indexOf(frequency)),
        createdAt = createdAt,
        reminderTimeStamp = reminderTimeStamp,
        category = category,
        note = note,
        color = color,
        isImportant = isImportant,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        longestStreakDayOfYear = longestStreakDayOfYearAndYear?.let{it.substringBefore("/").toInt()},
        longestStreakYear = longestStreakDayOfYearAndYear?.let{it.substringAfter("/").toInt()}
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        explain = explain,
        days = days.let{
            if(frequency==HabitFrequency.MONTHLY) days.map { "->$it<-" }.joinToString ( "||" )
            else days.map { "-$it-" }.joinToString ( "||" )
        },
        isActive = isActive,
        frequency = frequency.turkishName,
        createdAt = createdAt,
        reminderTimeStamp = reminderTimeStamp,
        category = category,
        note = note,
        color = color,
        isImportant = isImportant,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        longestStreakDayOfYearAndYear=longestStreakDayOfYear?.let{day->longestStreakYear?.let{year->"$day/$year"}}
    )
}
