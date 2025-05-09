package com.taner.taskly.data.repository

import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.data.local.dao.HabitCheckDao
import com.taner.taskly.data.local.dao.HabitDao
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.getExceptedDays
import java.util.Calendar

class HabitCheckRepository(private val dao: HabitCheckDao, private val hDao: HabitDao) {

    suspend fun insertCheck(check: HabitCheck) {
        dao.insertCheck(check.toEntity())
    }

    suspend fun update(habitId: Int, dayOfYear: Int, year: Int, note:String?) {
        dao.getHabitCheck(habitId,dayOfYear, year)?.let {
            dao.insertCheck(it.copy(note = note?: ""))
        }
    }


    suspend fun getCheckForDay(habitId: Int, dayOfYear: Int, year: Int): HabitCheck? {
        return dao.getCheckForDay(habitId, dayOfYear, year)?.toDomain()
    }

    suspend fun getChecksForDays(habitId: Int, dayOfYears: List<Int>, years: List<Int>): List<HabitCheckEntity?> {
        return dao.getChecksForDays(habitId, dayOfYears, years)
    }

    suspend fun getChecksForHabit(habitId: Int): List<HabitCheck> {
        return dao.getChecksForHabit(habitId).map { it.toDomain() }
    }

    suspend fun getTodayProgress(daysOfWeek: String, daysOfMonth: String, dayOfYear: Int, year: Int): CountsResult {
        return dao.getTodayProgress(daysOfWeek,daysOfMonth,dayOfYear,year)
    }

    suspend fun getTodayChecksForHabits(habitIds: List<Int>, dayOfYear: Int, year: Int): List<HabitCheck> {
        return dao.getTodayChecksForHabits(habitIds, dayOfYear, year).map { it.toDomain() }
    }

    suspend fun getCheckedCount(habitId: Int): Int {
        return dao.getCheckedCount(habitId)
    }

    suspend fun getChecksFromLastYear(habitId: Int,
                                      lastYear: Int,
                                      currentYear: Int,
                                      startDayOfYear: Int,
                                      currentDayOfYear: Int
    ): List<HabitCheckEntity>? {
        return dao.getChecksFromLastYear(habitId,lastYear, currentYear, startDayOfYear, currentDayOfYear)
    }

    suspend fun getChecksFromDateRange(habitId: Int,
                                       startYear: Int,
                                       endYear: Int,
                                       startDayOfYear: Int,
                                       endDayOfYear: Int
    ): List<HabitCheckEntity>? {
        return dao.getChecksFromDateRange(habitId, startYear, endYear, startDayOfYear, endDayOfYear)
    }


    suspend fun calculateSuccessRate(habitId: Int): Int {

        val checkedCount = dao.getCheckedCount(habitId)
        val habit = hDao.getHabitById(habitId)

        var expectedDays = 0


        habit?.toDomain()?.apply {

            expectedDays = getExceptedDays(this)





            return ((checkedCount.toDouble() / expectedDays) * 100).toInt()
        }


        return 0
    }


    suspend fun deleteCheck(check: HabitCheck) {
        dao.deleteCheck(check.toEntity())
    }

    suspend fun deleteCheck(habitId: Int, dayOfYear: Int, year: Int) {
        dao.deleteCheck(habitId,dayOfYear,year)
    }

    suspend fun deleteChecksBefore(habitId: Int, dayOfYear: Int, year: Int) {
        dao.deleteChecksBefore(habitId,dayOfYear,year)
    }

    suspend fun deleteAllForHabit(habitId: Int) {
        dao.deleteChecksForHabit(habitId)
    }

    suspend fun getLastHabitCheck(habitId: Int): HabitCheckEntity?{
       return dao.getLastHabitCheck(habitId)
    }

    suspend fun getAverageCompletionTime(habitId: Int): Long? {
        return dao.getAverageCompletionTime(habitId)
    }
}
