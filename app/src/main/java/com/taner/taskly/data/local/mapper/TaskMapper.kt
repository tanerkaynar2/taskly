package com.taner.taskly.data.local.mapper

import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskStatus

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        date = date,
        days = days,
        time = time,
        category = category,
        customCategoryDetail = customCategoryDetail,
        status = status,
        priority = TaskPriority.entries[priority],
        notification = notification,
        repetition = repetition,
        locations = locations,
        subTasks = subTasks,
        color = color,
        attachments = attachments,
        isCompleted = isCompleted,
        lastCompletedDate = lastCompletedDate,
        lastNotificationTime = lastNotificationTime,
        notificationDelayMin = notificationDelayMin
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        days = days,
        time = time,
        category = category,
        customCategoryDetail = customCategoryDetail,
        status = status,
        priority = priority.ordinal,
        notification = notification,
        repetition = repetition,
        locations = locations,
        subTasks = subTasks,
        color = color,
        attachments = attachments,
        isCompleted = isCompleted,
        lastCompletedDate = lastCompletedDate,
        lastNotificationTime = lastNotificationTime,
        notificationDelayMin = notificationDelayMin
    )
}
