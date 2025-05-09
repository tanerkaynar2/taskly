package com.taner.taskly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taner.taskly.data.local.dao.HabitCheckDao
import com.taner.taskly.data.local.dao.HabitDao
import com.taner.taskly.data.local.dao.NotificationDao
import com.taner.taskly.data.local.dao.TaskDao
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.entity.NotificationEntity
import com.taner.taskly.data.local.entity.TaskEntity


@Database(entities = [TaskEntity::class, HabitEntity::class,
    NotificationEntity::class, HabitCheckEntity::class],
    version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun notificationDao(): NotificationDao
    abstract fun habitCheckDao(): HabitCheckDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskly_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
