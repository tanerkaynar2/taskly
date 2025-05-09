package com.taner.taskly.data.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.taner.taskly.MainActivity
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.shouldResetTask
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.local.dao.TaskDao
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class TaskRepository(private val taskDao: TaskDao) {


    // Görev ekleme
    suspend fun addTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    // Görev güncelleme
    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun delTask(task: TaskEntity) {
        taskDao.delTask(task)
    }

    // Belirli bir kategoriye göre görevleri alma
    suspend fun getTasksByCategory(category: TaskCategory): List<TaskEntity> {
        return taskDao.getTasksByCategory(category)
    }

    suspend fun deleteAllTasks(){
        taskDao.deleteAllTasks()
    }

    suspend fun getTasksPaginated(limit: Int, offset: Int): List<TaskEntity> {
        return taskDao.getTasksPaginated(limit, offset)
    }

    // Görev ID'sine göre görev alma
    suspend fun getTaskById(id: Int): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    //1,5,7 gibi
    suspend fun getTasksByIds(ids: List<Int>): List<TaskEntity>? {
        return taskDao.getTasksByIds(ids)
    }
    suspend fun getLastTask(): TaskEntity? {
        return taskDao.getLastTask()
    }

    // Tamamlanan görev sayısını alma
    suspend fun getCompletedTaskCount(): Int {
        return taskDao.getCompletedTaskCount()
    }

    suspend fun getTaskCount(): Int {
        return taskDao.getTaskCount()
    }


    suspend fun deleteTasksOlderThanTwoDays(twoDaysAgo: Long) {
        taskDao.deleteTasksOlderThanTwoDays(twoDaysAgo)
    }

    suspend fun getTodayTasks(startOfDay: Long, endOfDay: Long,dayName: String , todayDayIndex: Int): List<TaskEntity>  {
        return taskDao.getTodayTasks(startOfDay,endOfDay,dayName, todayDayIndex)
    }

    suspend fun getTasksByDate(startOfDay: Long, endOfDay: Long,dayName: String , dayIndex: Int): List<TaskEntity>  {
        return taskDao.getTasksByDate(startOfDay,endOfDay,dayName, dayIndex)
    }

    suspend fun getTasksByDateAndNotification(startOfDay: Long, endOfDay: Long,dayName: String , dayIndex: Int): List<TaskEntity>  {
        return taskDao.getTasksByDate(startOfDay,endOfDay,dayName, dayIndex, true)
    }



    suspend fun runAnalysis(context: Context) {


        val calendar = Calendar.getInstance().apply {
            timeInMillis = Date().time
        }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = (calendar.get(Calendar.YEAR))
        val dayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7 else calendar.get(Calendar.DAY_OF_WEEK) - 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        var pastTaskReminder = true

        context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
            pastTaskReminder = getBoolean("pastTaskReminder",true)
        }
        var nowTaskReminder = true

        context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
            nowTaskReminder = getBoolean("nowTaskReminder",true)
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
        val dao = db.notificationDao()


        suspend fun syncNotDatabase(){

            dao.deleteOldShownNotifications(dayOfYear, year)

        }


       /* withContext(Dispatchers.Main){
            Toast.makeText(context, "$dayOfYear $dayOfWeek $dayOfMonth $year}", Toast.LENGTH_SHORT).show()

        }*/

        if(pastTaskReminder || nowTaskReminder){

            dao.apply {



                val nots = getTodayNotifications(dayOfWeek, dayOfMonth, dayOfYear, year)

                nots?.let{

                    val tasks = getTasksByIds(nots.map { it.taskId })

                    nots.forEach { not->

                        val task = tasks?.filter { it.id == not.taskId}?.firstOrNull()

                        task?.let{

                         /*   withContext(Dispatchers.Main){
                                Toast.makeText(context, "${nots?.size} $size", Toast.LENGTH_SHORT).show()

                            }*/
                            NotificationUtils().postNotification(context,it.toDomain(),null,not,nowTaskReminder, pastTaskReminder)
                        }
                        if(task == null){
                            dao.deleteNotificationsByTaskId(not.taskId)
                        }

                    }

                }

                syncNotDatabase()



            }
        }else {
            syncNotDatabase()

        }


    }
}
