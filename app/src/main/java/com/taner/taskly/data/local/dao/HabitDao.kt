package com.taner.taskly.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.Year
import java.time.YearMonth

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: HabitEntity)

    @Delete
    suspend fun delHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits order by id desc limit 1")
    suspend fun getLastHabit(): HabitEntity?

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE frequency = :frequency ORDER BY name ASC")
    suspend fun getHabitsByFrequency(frequency: HabitFrequency): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): HabitEntity?

    @Query("SELECT * FROM habits WHERE id in (:ids)")
    suspend fun getHabitByIds(ids: List<Int>): List<HabitEntity>?



    @Query("SELECT * FROM habits ORDER BY longestStreak DESC LIMIT :count")
    suspend fun getLongestStreak(count: Int): List<HabitEntity>?



    @Query("SELECT * FROM habits WHERE reminderTimeStamp is not null and isActive = 1 and (days LIKE '%' || :dayOfWeek || '%' or days LIKE '%' || :dayOfMonth || '%')")
    suspend fun getHabitsByTodayNotifications(dayOfWeek: String, dayOfMonth: String): List<HabitEntity>

    @Query("SELECT * FROM habits Where isActive = 1 AND ((days LIKE '%' || :dayOfWeek || '%') or days LIKE '%' || :dayOfMonth || '%') ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getActiveHabitsPaginated(limit: Int, offset: Int, dayOfWeek: String, dayOfMonth: String): List<HabitEntity>

    @Query("SELECT * FROM habits Where isActive = 0 AND (days LIKE '%' || :dayOfWeek || '%' or days LIKE '%' || :dayOfMonth || '%') ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getInActiveHabitsPaginated(limit: Int, offset: Int, dayOfWeek: String, dayOfMonth: String): List<HabitEntity>

    @Query("SELECT * FROM habits Where isActive = 1 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllActiveHabitsPaginated(limit: Int, offset: Int): List<HabitEntity>


    @Query("SELECT * FROM habits Where isActive = 1 and ( ((days LIKE '%' || :dayOfWeek || '%') or days LIKE '%' || :dayOfMonth || '%')) ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllActiveHabitsPaginated(limit: Int, offset: Int,dayOfWeek: String, dayOfMonth:String): List<HabitEntity>

    @Query("SELECT * FROM habits Where isActive = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllInActiveHabitsPaginated(limit: Int, offset: Int): List<HabitEntity>


    @Query("SELECT * FROM habits Where isActive = 0 and ( ((days LIKE '%' || :dayOfWeek || '%') or days LIKE '%' || :dayOfMonth || '%')) ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllInActiveHabitsPaginated(limit: Int, offset: Int,dayOfWeek: String, dayOfMonth:String): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

}
