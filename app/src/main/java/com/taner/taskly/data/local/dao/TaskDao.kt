package com.taner.taskly.data.local.dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun delTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY date ASC")
    suspend fun getTasksByCategory(category: TaskCategory): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Sayfalama (LIMIT ve OFFSET kullanarak)
    @Query("SELECT * FROM tasks ORDER BY date ASC LIMIT :limit OFFSET :offset")
    suspend fun getTasksPaginated(limit: Int, offset: Int): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE " +

            "((date BETWEEN :startOfDay AND :endOfDay) or (date is null and days is null) or (repetition = 'DAILY') or (days is not null and (days LIKE '%' || :dayName || '%' or days LIKE '%->' || :todayDayIndex || '%')))"

            +" ORDER BY CASE WHEN time IS NULL THEN 1 ELSE 0 END, time ASC")
    suspend fun getTodayTasks(startOfDay: Long, endOfDay: Long,dayName: String , todayDayIndex: Int): List<TaskEntity>


    @Query("SELECT * FROM tasks WHERE " +

            "(((date BETWEEN :startOfDay AND :endOfDay) or (date is null and days is null) " +
            "or (repetition = 'DAILY') or (days is not null and (days LIKE '%' || :dayName || '%' or days LIKE '%->' || :dayIndex || '%'))) )"


            +" ORDER BY CASE WHEN time is null THEN 1 else 0 end, time ASC")
    suspend fun getTasksByDate(startOfDay: Long, endOfDay: Long,dayName: String , dayIndex: Int ,):List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE " +

            "((((date BETWEEN :startOfDay AND :endOfDay) or (date is null and days is null) " +
            "or (repetition = 'DAILY') or (days is not null and (days LIKE '%' || :dayName || '%' or days LIKE '%->' || :dayIndex || '%'))) ) and notification = :notification )"


            +" ORDER BY CASE WHEN time is null THEN 1 else 0 end, time ASC")
    suspend fun getTasksByDate(startOfDay: Long, endOfDay: Long, dayName: String, dayIndex: Int, notification: Boolean):  List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id in (:ids)")
    suspend fun getTasksByIds(ids: List<Int>): List<TaskEntity>?

    @Query("SELECT * FROM tasks order by id desc limit 1")
    suspend fun getLastTask(): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks ")
    suspend fun getTaskCount(): Int

    @Query("DELETE FROM tasks WHERE repetition = 'NONE' AND date < :twoDaysAgo")
    suspend fun deleteTasksOlderThanTwoDays(twoDaysAgo: Long)



}
