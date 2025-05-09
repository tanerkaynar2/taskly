package com.taner.taskly.data.local.dao

import androidx.room.*
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.HabitCheck

@Dao
interface HabitCheckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(habitCheck: HabitCheckEntity)


    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId AND dayOfYear = :dayOfYear AND year = :year")
    suspend fun getCheckForDay(habitId: Int, dayOfYear: Int, year: Int): HabitCheckEntity?

    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId AND dayOfYear in (:dayOfYears) AND year in (:years)")
    suspend fun getChecksForDays(habitId: Int, dayOfYears: List<Int>, years: List<Int>): List<HabitCheckEntity?>

    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId")
    suspend fun getChecksForHabit(habitId: Int): List<HabitCheckEntity>


    @Query("""
        SELECT 
          (SELECT COUNT(*) FROM habits WHERE (days like '%' || :daysOfWeek || '%' or days like '%' || :daysOfMonth || '%') and isActive = 1) AS totalCount,
          (SELECT COUNT(*) FROM habit_checks WHERE dayOfYear = :dayOfYear and year = :year and isChecked = 1) AS completedCount
    """)
    suspend fun  getTodayProgress(daysOfWeek: String, daysOfMonth: String, dayOfYear: Int, year: Int): CountsResult

    @Query("SELECT * FROM habit_checks WHERE habitId in (:habitIds) ")
    suspend fun getChecksForHabits(habitIds: List<Int>): List<HabitCheckEntity>

    @Query("SELECT * FROM habit_checks WHERE habitId in (:habitIds) and dayOfYear = :dayOfYear and year = :year")
    suspend fun getTodayChecksForHabits(habitIds: List<Int>, dayOfYear: Int, year: Int): List<HabitCheckEntity>

    @Query("SELECT * FROM habit_checks WHERE habitId in (:habitIds) and dayOfYear = :dayOfYear and year = :year")
    suspend fun getHabitCheck(habitIds: Int, dayOfYear: Int, year: Int): HabitCheckEntity?

    @Query("SELECT COUNT(*) FROM habit_checks WHERE habitId = :habitId AND isChecked = 1")
    suspend fun getCheckedCount(habitId: Int): Int

    @Query("SELECT COUNT(*) FROM habit_checks WHERE habitId = :habitId")
    suspend fun getTotalCheckCount(habitId: Int): Int



    @Query("""
    SELECT * FROM habit_checks 
    WHERE 
        (year * 366 + dayOfYear) >= (:currentYear * 366 + :currentDayOfYear - 60)
""")
    suspend fun getLast60DaysChecks(currentDayOfYear: Int, currentYear: Int): List<HabitCheckEntity>

    @Query("""
        SELECT * FROM habit_checks
        WHERE 

        habitId = :habitId
            And ((year = :lastYear AND dayOfYear >= :startDayOfYear) OR
            (year = :currentYear AND dayOfYear <= :currentDayOfYear))
    """)
    suspend fun getChecksFromLastYear(habitId: Int,
        lastYear: Int,
        currentYear: Int,
        startDayOfYear: Int,
        currentDayOfYear: Int
    ): List<HabitCheckEntity>?

    @Query("""
        SELECT * FROM habit_checks
        WHERE 

        habitId = :habitId
            And ((year>:startYear OR (year=:startYear AND dayOfYear>=:startDayOfYear)) AND 
            (year<:endYear OR (year=:endYear AND dayOfYear<=:endDayOfYear))

)
    """)
    suspend fun getChecksFromDateRange(habitId: Int,
        startYear: Int,
        endYear: Int,
        startDayOfYear: Int,
        endDayOfYear: Int
    ): List<HabitCheckEntity>?


    @Query("""
    SELECT * FROM habit_checks 
    WHERE habitId = :habitId 
    ORDER BY year DESC, dayOfYear DESC 
    LIMIT 1
""")
    suspend fun getLastHabitCheck(habitId: Int): HabitCheckEntity?


    @Delete
    suspend fun deleteCheck(habitCheck: HabitCheckEntity)

    @Query("DELETE FROM habit_checks WHERE habitId = :habitId")
    suspend fun deleteChecksForHabit(habitId: Int)

    @Query("""
        DELETE FROM habit_checks 
        WHERE habitId = :habitId AND (year < :year OR (year = :year AND dayOfYear < :dayOfYear))
    """)
    suspend fun deleteChecksBefore(habitId: Int, dayOfYear: Int, year: Int)

    @Query("DELETE FROM habit_checks WHERE habitId = :habitId and dayOfYear = :dayOfYear and year = :year")
    suspend fun deleteCheck(habitId:Int, dayOfYear: Int, year: Int)


    @Query("SELECT AVG(time) FROM habit_checks WHERE habitId = :habitId AND isChecked = 1")
    suspend fun getAverageCompletionTime(habitId: Int): Long?

}
