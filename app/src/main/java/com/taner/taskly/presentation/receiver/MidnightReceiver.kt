package com.taner.taskly.presentation.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.icu.util.Calendar
import androidx.room.Room
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.repository.HabitRepository
import com.taner.taskly.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {



        NotificationUtils.setReminderAlarm(context)

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "task_db"
        ).build()

        val taskDao = db.taskDao()
        val habitDao = db.habitDao()
        val habitCheckDao = db.habitCheckDao()

        val taskRepository = TaskRepository(taskDao)
        val habitRepository = HabitRepository(habitDao,habitCheckDao)

        CoroutineScope(Dispatchers.IO).launch {


            taskRepository.runAnalysis(context)
            habitRepository.runAnalysis(context)

        }



        context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
            edit().putLong("is_setted_midnight_receiver",System.currentTimeMillis()).apply()


            val lastCallDailyReminderTime = getInt("lastCallDailyReminderTime",-1)
            if(lastCallDailyReminderTime == -1 || lastCallDailyReminderTime.let{
                it != Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }.get(Calendar.DAY_OF_YEAR)
                } ){
                getString("dailyReminderTime",null)?.let{
                    NotificationUtils.dailyRemember(context,it)
                }
            }
        }
    }
}
