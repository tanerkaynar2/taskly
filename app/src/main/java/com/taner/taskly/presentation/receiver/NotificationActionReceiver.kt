package com.taner.taskly.presentation.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.taner.taskly.MainActivity.Companion.channelId
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.repository.HabitCheckRepository
import com.taner.taskly.data.repository.HabitRepository
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            "MARK_AS_DELAY" -> {
                val taskId = intent.getIntExtra("taskId", -1)
                val notificationTimestamp = intent.getStringExtra("notificationTimestamp") ?: ""
                val delayTime = intent.getStringExtra("delayTime") ?: "60"
                if (taskId != -1) {


                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var channelId = channelId + "_$taskId"
                    notificationManager.cancel(taskId)

                    // Görevi tamamlandı olarak işaretle
                    CoroutineScope(Dispatchers.IO).launch {
                        //val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                        //val dao = db.taskDao()
                        //val task = dao.getTaskById(taskId)
                        //dao.updateTask(it.copy(notificationDelayMin = delayTime.toInt(), lastNotificationTime = null))

                        notificationTimestamp.toLongOrNull()?.let{ti->

                            NotificationUtils.delayNotificationToDatabase(context,taskId,delayTime.toInt())

                        }
                    }
                }
            }
            "MARK_AS_COMPLETED" -> {
                val taskId = intent.getIntExtra("taskId", -1)
                if (taskId != -1) {

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var channelId = channelId + "_$taskId"
                    notificationManager.cancel(taskId)

                    // Görevi tamamlandı olarak işaretle
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                        val dao = db.taskDao()
                        val task = dao.getTaskById(taskId)
                        task?.let {

                            dao.updateTask(it.copy(isCompleted = true, status = TaskStatus.COMPLETED, lastCompletedDate = dateFormat3.format(
                                Date()
                            )))
                            val notDao = db.notificationDao()
                            notDao.deleteNotificationsByTaskId(taskId)

                        }
                    }
                }
            }
            "COMPLETE_HABIT" -> {
                val habitId = intent.getIntExtra("habitId", -1)
                if (habitId != -1) {

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var channelId = "habit_channel_$habitId"
                    notificationManager.cancel(habitId)

                    CoroutineScope(Dispatchers.IO).launch {
                        val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                        val dao = db.habitDao()
                        val checkDao = db.habitCheckDao()
                        val repository = HabitRepository(dao,checkDao)
                        val cRepository = HabitCheckRepository(checkDao,dao)
                        val habit = dao.getHabitById(habitId)
                        val viewModel = HabitViewModel(repository,cRepository)
                        habit?.let {

                            viewModel.toggleHabit(habit, context)


                        }
                    }
                }
            }
            "MARK_AS_IN_PROGRESS" -> {
                val taskId = intent.getIntExtra("taskId", -1)
                if (taskId != -1) {

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var channelId = channelId + "_$taskId"
                    notificationManager.cancel(taskId)

                    // Görevi tamamlandı olarak işaretle
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                        val dao = db.taskDao()
                        val task = dao.getTaskById(taskId)
                        task?.let {

                            dao.updateTask(it.copy(status = TaskStatus.IN_PROGRESS))

                        }
                    }
                }
            }
        }
    }
}
