package com.taner.taskly.presentation.habit

import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taner.taskly.core.utils.AchievementUtils
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.getHourAndMinuteFromTimestamp
import com.taner.taskly.core.utils.DateUtils.Companion.isTimeInRange
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.repository.HabitCheckRepository
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashMap

class HabitCheckViewModel(private val repository: HabitCheckRepository) : ViewModel() {

    private val _habitChecks = MutableStateFlow<List<HabitCheck>>(emptyList())
    val habitChecks: StateFlow<List<HabitCheck>> = _habitChecks




    fun loadChecksForHabit(habitId: Int) {
        viewModelScope.launch {
            val result = repository.getChecksForHabit(habitId)
            _habitChecks.value = result
        }
    }

    fun toggleCheck(habitId: Int, dayOfYear: Int, year: Int, isChecked: Boolean,note:String?=null) {
        viewModelScope.launch {
            repository.insertCheck(
                HabitCheck(
                    habitId = habitId,
                    dayOfYear = dayOfYear,
                    year = year,
                    isChecked = isChecked,
                    time = timeFormat.format(Date()).let{
                        DateUtils.getTimeOnlyTimestamp(it.substringBefore(":").toInt(),it.substringAfter(":").toInt())
                    }
                ).let{
                    if(note!=null) it.copy(note = note) else it
                }
            )
            loadChecksForHabit(habitId)
        }
    }



    fun getHabitAchievement(habit: Habit,achUtils: AchievementUtils,sp:SharedPreferences ,result: (
        List<Pair<String, AchievementUtils. Achievement?>>?)->Unit){
        viewModelScope.launch {


            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val now = LocalDate.now()


                    val oneYearAgo = now.minusYears(1)

                    val checksFromLastYear = (getChecksFromLastYear(habit.id,
                        lastYear = oneYearAgo.year,
                        currentYear = now.year,
                        startDayOfYear = oneYearAgo.dayOfYear,
                        currentDayOfYear = now.dayOfYear
                    )?: emptyList()).sortedWith(compareBy({ it.year }, { it.dayOfYear }))


                    fun groupByWeekWithDays(habitChecks: List<HabitCheckEntity>): Map<Int, List<Pair<Int, HabitCheck>>> {
                        val calendar = Calendar.getInstance()

                        val grouped = mutableMapOf<Int, MutableList<Pair<Int, HabitCheck>>>()

                        habitChecks.forEach { entity ->
                            calendar.timeInMillis = entity.time

                            val week = calendar.get(Calendar.WEEK_OF_YEAR)
                            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).let{
                                if(it==1) 7 else it-1
                            }

                            val habit = entity.toDomain()
                            val dayName = dayOfWeek

                            grouped.computeIfAbsent(week) { mutableListOf() }
                                .add(dayName to habit)
                        }

                        return grouped.toSortedMap()
                    }



                    val grouped = groupByWeekWithDays(checksFromLastYear)


                    val weeklyStats = mutableListOf<Int>()
                    val hourCompletedCounts = mutableListOf<Pair<Int,Int>>()
                    var morningMarks = 0
                    var beforeBreakfastMarks = 0
                    var workMarks = 0
                    var marksWithin5Min = 0
                    var marksWithin10Min = 0
                    var marksBeforeNoon = 0
                    var marksBeforeMidnight = 0
                    var nightMarksStreak = 0
                    var beforeNightMarkStreak = false
                    grouped.forEach { t, u ->
                        weeklyStats.add(u.count { it.second.isChecked })

                        u.forEach {
                            val timee = getHourAndMinuteFromTimestamp(it.second.time)
                            val hour = timee.first
                            val min = timee.second

                            val before = (hourCompletedCounts.filter { it.first == hour }.firstOrNull())
                            before?.let { hourCompletedCounts.remove(it) }
                            hourCompletedCounts.add(before?.let {
                                before.first to (before.first + 1)
                            } ?: (hour to 1))


                            if(it.second.isChecked){
                                if(isTimeInRange(hour, min, 5, 0, 9, 59) ){
                                    morningMarks++
                                    marksBeforeNoon++
                                    marksBeforeMidnight++
                                    beforeBreakfastMarks++
                                }else if(isTimeInRange(hour, min, 0, 0, 5, 0) ){
                                    beforeBreakfastMarks++
                                    marksBeforeNoon++
                                }else if(isTimeInRange(hour, min, 10, 0, 13, 0) ){
                                    marksBeforeMidnight++
                                    marksBeforeNoon++
                                }else if(isTimeInRange(hour, min, 13, 0, 23, 59) ){
                                    marksBeforeMidnight++
                                }

                                if(it.first in 1..5){
                                    if(isTimeInRange(hour, min, 8, 0, 18, 0) ){
                                        workMarks++
                                    }
                                }

                                if(beforeNightMarkStreak){
                                    if(isTimeInRange(hour, min, 5, 0, 23, 59) ){
                                        nightMarksStreak++
                                        beforeNightMarkStreak = true
                                    }else beforeNightMarkStreak = false
                                }

                            }else{
                                beforeNightMarkStreak = false
                            }





                        }

                    }


                    val userProgress = AchievementUtils.UserProgress(
                        totalMarkedDays=checksFromLastYear.size,
                     maxStreak=habit.longestStreak,
                     currentStreak=habit.currentStreak,
                     weeklyStats=weeklyStats,
                     morningMarks=morningMarks,
                     beforeBreakfastMarks=beforeBreakfastMarks,
                     workMarks=workMarks,
                     marksWithin5Min=marksWithin5Min,
                     marksWithin10Min=marksWithin10Min,
                     marksBeforeNoon=marksBeforeNoon,
                     marksBeforeMidnight=marksBeforeMidnight,
                     nightMarksStreak=nightMarksStreak,
                        hourCompletedCounts = hourCompletedCounts.sortedBy { it.first }
                    )


                    achUtils.getHighestDailyAchievement(progress = userProgress, sp = sp).let {
                        withContext(Dispatchers.Main){
                            result.invoke(it)
                        }
                    }




                } else {
                    withContext(Dispatchers.Main){
                        result.invoke(null)
                    }
                }
            }
            catch (e: Exception) {
            }

        }

    }

    fun deleteCheck(check: HabitCheck) {
        viewModelScope.launch {
            repository.deleteCheck(check)
            loadChecksForHabit(check.habitId)
        }
    }


    fun deleteCheck(habitId:Int, dayOfYear: Int, year: Int) {
        viewModelScope.launch {
            repository.deleteCheck(habitId,dayOfYear,year)
        }
    }
    fun deleteChecksBefore(habitId: Int, dayOfYear: Int, year: Int) {
        viewModelScope.launch {
            repository.deleteChecksBefore(habitId,dayOfYear,year)
        }
    }

    fun deleteAllForHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteAllForHabit(habitId)
            _habitChecks.value = emptyList()
        }
    }

    suspend fun getLastHabitCheck(habitId: Int): HabitCheckEntity? {
        return repository.getLastHabitCheck(habitId)
    }


    suspend fun getChecksFromLastYear(habitId: Int,
                                      lastYear: Int,
                                      currentYear: Int,
                                      startDayOfYear: Int,
                                      currentDayOfYear: Int
    ): List<HabitCheckEntity>? {
        return repository.getChecksFromLastYear(habitId,lastYear, currentYear, startDayOfYear, currentDayOfYear)
    }
    suspend fun getChecksFromDateRange(habitId: Int,
                                       startYear: Int,
                                       endYear: Int,
                                       startDayOfYear: Int,
                                       endDayOfYear: Int
    ): List<HabitCheckEntity>? {
        return repository.getChecksFromDateRange(habitId, startYear, endYear, startDayOfYear, endDayOfYear)
    }



    suspend fun calculateSuccessRate(habitId: Int): Int {
        return repository.calculateSuccessRate(habitId)
    }

    suspend fun getAverageCompletionTime(habitId: Int): Long? {
        return repository.getAverageCompletionTime(habitId)
    }

    suspend fun getCheckedCount(habitId: Int): Int {
        return repository.getCheckedCount(habitId)
    }

    fun update(habitId: Int, dayOfYear: Int, year: Int, note:String?) {
        viewModelScope.launch {
            repository.update(habitId,dayOfYear, year, note)

        }
    }


    fun getCheckForDay(habitId: Int, dayOfYear: Int, year: Int, onResult: (HabitCheck?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getCheckForDay(habitId, dayOfYear, year)
            onResult(result)
        }
    }


    fun getChecksForDays(habitId: Int, dayOfYears: List<Int>, years: List<Int>, onResult: (List<HabitCheckEntity?>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getChecksForDays(habitId, dayOfYears, years)
            onResult(result)
        }
    }

    suspend fun getChecksForDays(habitId: Int, dayOfYears: List<Int>, years: List<Int>,) :List<HabitCheckEntity?>{
        val result = repository.getChecksForDays(habitId, dayOfYears, years)
        return result
    }


}
