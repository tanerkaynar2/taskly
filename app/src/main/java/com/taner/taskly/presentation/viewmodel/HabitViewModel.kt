package com.taner.taskly.presentation.viewmodel

import android.content.Context
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.repository.HabitRepository
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.data.repository.HabitCheckRepository
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.domain.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Year
import java.util.Date

class HabitViewModel(val habitRepository: HabitRepository, val habitCheckRepository: HabitCheckRepository) : ViewModel() {

    companion object{
        val habitChecks : HashMap<Int, HabitCheck> = hashMapOf()
        val habitChecksByDayOfYearAndYear : HashMap<Int, List<HabitCheck>> = hashMapOf()
        val habitNotCheckChecksByDayOfYearAndYear : HashMap<Int, List<Pair<Int, Int>>> = hashMapOf()

        var isInitHabitSection = false

        var dayOfYear = 0
        var year = 0
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val dayOfWeek = ((android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
        val dayOfMonth = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_MONTH)   // 1-31
        val monthIndex = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.MONTH)


        data class NotificationThreshold(
            val controlPeriodDays: Int, // Kaç günlük periyotta kontrol edilecek
            val maxMissedCount: Int // Maksimum kaç gün/günlük görev kaçırılabilir
        )

        fun getNotificationThreshold(
            frequency: HabitFrequency,
            isImportant: Boolean,
            selectedDaysOrDates: List<Int>
        ): NotificationThreshold {
            return when (frequency) {
                HabitFrequency.DAILY -> {
                    if (isImportant) {
                        NotificationThreshold(
                            controlPeriodDays = 5,
                            maxMissedCount = 2
                        )
                    } else {
                        NotificationThreshold(
                            controlPeriodDays = 7,
                            maxMissedCount = 5
                        )
                    }
                }

                HabitFrequency.WEEKLY -> {
                    val selectedCount = selectedDaysOrDates.size
                    if (isImportant) {
                        NotificationThreshold(
                            controlPeriodDays = 7,
                            maxMissedCount = (selectedCount * 0.3).toInt().coerceAtLeast(1)
                        )
                    } else {
                        NotificationThreshold(
                            controlPeriodDays = 14,
                            maxMissedCount = (selectedCount * 2 * 0.5).toInt().coerceAtLeast(1)
                        )
                    }
                }

                HabitFrequency.MONTHLY -> {
                    val selectedCount = selectedDaysOrDates.size
                    if (isImportant) {
                        NotificationThreshold(
                            controlPeriodDays = 30,
                            maxMissedCount = (selectedCount * 0.3).toInt().coerceAtLeast(1)
                        )
                    } else {
                        NotificationThreshold(
                            controlPeriodDays = 60,
                            maxMissedCount = (selectedCount * 2 * 0.5).toInt().coerceAtLeast(1)
                        )
                    }
                }
            }
        }


        fun getExceptedDays(habit: Habit): Int {

            val habitCalendar = Calendar.getInstance().apply { timeInMillis = habit.createdAt }
            val habitYear = habitCalendar.get(Calendar.YEAR)
            val habitDayOfYear = habitCalendar.get(Calendar.DAY_OF_YEAR)

            habit.apply {
                (Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(java.util.Calendar.HOUR_OF_DAY,0)
                    set(java.util.Calendar.MINUTE,0)
                    set(java.util.Calendar.SECOND,0)
                    set(java.util.Calendar.MILLISECOND,0)
                }.timeInMillis - java.util.Calendar.getInstance().apply {
                    timeInMillis = createdAt
                    set(java.util.Calendar.HOUR_OF_DAY,0)
                    set(java.util.Calendar.MINUTE,0)
                    set(java.util.Calendar.SECOND,0)
                    set(java.util.Calendar.MILLISECOND,0)
                }.timeInMillis).let { diffInMillis->
                    val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                    return if(frequency == HabitFrequency.MONTHLY){
                        val scheduledDays = this.days.map { it - 1 }.toSet()
                        var total =  DateUtils.countSpecificMonthDaysSinceCreated(createdAt, scheduledDays)

                        if(habitYear< year){
                            val diffYear = year-habitYear
                            val dayOfYearRanges = mutableListOf(habitDayOfYear..habitCalendar.getActualMaximum (Calendar.DAY_OF_YEAR))
                            for(i in 1 until diffYear){
                                habitCalendar.add(Calendar.YEAR,1)
                                dayOfYearRanges.add(1..habitCalendar.getActualMaximum (Calendar.DAY_OF_YEAR))
                            }
                            dayOfYearRanges.add(1..dayOfYear)

                            total = 0
                            habitCalendar.timeInMillis = habit.createdAt
                            dayOfYearRanges.forEach {
                                habitCalendar.set(Calendar.DAY_OF_YEAR,it.first())
                                val start = habitCalendar.timeInMillis
                                habitCalendar.set(Calendar.DAY_OF_YEAR,it.last())
                                val end = habitCalendar.timeInMillis
                                total += DateUtils.countSpecificMonthDaysSinceCreated(start= start, scheduledDays,end=end)
                                habitCalendar.add(Calendar.YEAR,1)
                            }
                        }

                        val habitCalendar = Calendar.getInstance().apply { timeInMillis = habit.createdAt }
                        val dayOfMonth = habitCalendar.get(Calendar.DAY_OF_MONTH)
                        val diff = habit.days.size - habit.days.filter { it>=dayOfMonth }.size
                        total-=diff

                        if(total == 0) return 0 else total
                    }else if(frequency == HabitFrequency.WEEKLY){
                        val scheduledDays = this.days.map { it - 1 }.toSet()
                        var total =  DateUtils.countSpecificWeekDaysSinceCreated(createdAt, scheduledDays)

                        if(total == 0) return 0 else total
                    }else days + 1
                }
            }
        }

        fun getPreviousRepeatDay(today: Calendar, type: HabitFrequency, selectedDays: List<Int>): Calendar {
            val copy = today.clone() as Calendar
            copy.set(Calendar.HOUR_OF_DAY, 0)
            copy.set(Calendar.MINUTE, 0)
            copy.set(Calendar.SECOND, 0)
            copy.set(Calendar.MILLISECOND, 0)

            return when (type) {
                HabitFrequency.DAILY -> {
                    copy.add(Calendar.DAY_OF_YEAR, -1)
                    copy
                }

                HabitFrequency.WEEKLY -> {
                    val todayIndex = (today.get(Calendar.DAY_OF_WEEK) + 5) % 7  // 0 = Pazartesi, 6 = Pazar
                    val sortedDays = selectedDays.sorted()
                    val prevDay = sortedDays.lastOrNull { it < todayIndex } ?: sortedDays.last()

                    val daysBack = if (prevDay < todayIndex) todayIndex - prevDay else 7 - (prevDay - todayIndex)
                    copy.add(Calendar.DAY_OF_YEAR, -daysBack)
                    copy
                }

                HabitFrequency.MONTHLY -> {
                    val todayDay = today.get(Calendar.DAY_OF_MONTH)
                    val sortedDays = selectedDays.map { it + 1 }.sorted()  // çünkü 0 = 1’i temsil ediyordu
                    val prevDay = sortedDays.lastOrNull { it < todayDay }

                    if (prevDay != null) {
                        copy.set(Calendar.DAY_OF_MONTH, prevDay)
                    } else {
                        // geçen aya git
                        copy.add(Calendar.MONTH, -1)
                        val maxDay = copy.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val validDays = sortedDays.filter { it <= maxDay }
                        val newPrevDay = validDays.last()
                        copy.set(Calendar.DAY_OF_MONTH, newPrevDay)
                    }
                    copy
                }

                else -> throw IllegalArgumentException("Geçersiz tekrar tipi: $type")
            }
        }


    }

    // Günlük alışkanlıkları almak
    suspend fun getDailyHabits() = habitRepository.getHabitsByFrequency(HabitFrequency.DAILY)

    // Haftalık alışkanlıkları almak
    suspend fun getWeeklyHabits() = habitRepository.getHabitsByFrequency(HabitFrequency.WEEKLY)

    // Aylık alışkanlıkları almak
    suspend fun getMonthlyHabits() = habitRepository.getHabitsByFrequency(HabitFrequency.MONTHLY)

    suspend fun getHabitById(id: Int) = habitRepository.getHabitById(id)


    // Alışkanlık ekleme
    fun addHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.addHabit(habit)
            getLastHabit()?.let{habit->
                var calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }
                habitCheckRepository.insertCheck(HabitCheck(habitId = habit.id, dayOfYear =
                    if(habit.toDomain().frequency == HabitFrequency.MONTHLY){
                        val scheduledDays = habit.toDomain().days.sorted()

                        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                        val nextDayOfMonth = scheduledDays.firstOrNull { it >= today }
                            ?: scheduledDays.first()

                        calendar.set(Calendar.DAY_OF_MONTH, nextDayOfMonth)

                        calendar.get(Calendar.DAY_OF_YEAR)
                    }else if(habit.toDomain().frequency == HabitFrequency.WEEKLY){

                        val scheduledWeekDays = habit.toDomain().days.sorted()
                        val calendarDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        val todayIndex = (calendarDow + 5) % 7 + 1
                        val nextWeekDay = scheduledWeekDays.firstOrNull { it >= todayIndex }
                            ?: scheduledWeekDays.first()

                        calendar.set(Calendar.DAY_OF_WEEK, when (nextWeekDay) {
                            1 -> Calendar.MONDAY
                            2 -> Calendar.TUESDAY
                            3 -> Calendar.WEDNESDAY
                            4 -> Calendar.THURSDAY
                            5 -> Calendar.FRIDAY
                            6 -> Calendar.SATURDAY
                            else -> Calendar.SUNDAY
                        })
                        calendar.get(Calendar.DAY_OF_YEAR)

                    }else dayOfYear, year = calendar.get(Calendar.YEAR), isChecked = false,
                    time = timeFormat.format(Date()).let{
                        DateUtils.getTimeOnlyTimestamp(it.substringBefore(":").toInt(),it.substringAfter(":").toInt())
                    }))

            }

        }
    }






    suspend fun getLastHabit(): HabitEntity?{
        return habitRepository.getLastHabit()
    }

    suspend fun getLongestStreak(count: Int=4): List<HabitEntity>?{
        return habitRepository.getLongestStreak(count)
    }


    fun delHabit(habit: HabitEntity,context:Context,All:Boolean? = false) {
        viewModelScope.launch {
            habitCheckRepository.deleteAllForHabit(habit.id)
            habitRepository.delHabit(habit)

            if(habit.reminderTimeStamp!=null) NotificationUtils.cancelScheduledNotification(context, habitId = habit.id)
            if(All!= null) loadHabits(All=All)
        }
    }
    fun delHabit(id: Int,context: Context,All:Boolean? = false) {
        viewModelScope.launch {
            habitCheckRepository.deleteAllForHabit(id)
            habitRepository.getHabitById(id)?.let {habitRepository.delHabit(it) }
            NotificationUtils.cancelScheduledNotification(context, habitId = id)
            if(All!= null) loadHabits( All=All)
        }
    }
    fun activeToggleHabit(id: Int,All:Boolean? = false) {
        viewModelScope.launch {
            habitRepository.getHabitById(id)?.let {
                habitRepository.updateHabit(it.copy(isActive = !it.isActive))

            }
            if(All != null) loadHabits(All=All)


        }
    }
    fun toggleHabit(habit: HabitEntity,context: Context, loadHabitChecks: Boolean = false,All:Boolean? = false,
                    dayOfYear: Int?=null, year: Int?=null, markCheckedWhenIsEmpty: Boolean = false,addHabitCheckNote: String?=null
                    , loadHabitChecksByDayOfYearAndYear: Boolean = true ) {
        viewModelScope.launch {

            val today = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                dayOfYear?.let{set(Calendar.DAY_OF_YEAR,it)}
                year?.let{set(Calendar.YEAR,it)}
            }

            val year = today.get(Calendar.YEAR)
            val dayOfYear = today.get(Calendar.DAY_OF_YEAR)

            val check = habitCheckRepository.getCheckForDay(habitId = habit.id,year=year, dayOfYear = dayOfYear)




            var beforeDay = dayOfYear
            var beforeYear = year
            habit.toDomain().let { habit->
                val beforeDate = getPreviousRepeatDay(today,habit.frequency,habit.days.map { it-1 })
                beforeDay = beforeDate.get(Calendar.DAY_OF_YEAR)
                beforeYear = beforeDate.get(Calendar.YEAR)
            }




            val beforeCheck = habitCheckRepository.getCheckForDay(habitId = habit.id,
                year=beforeYear,
                dayOfYear = beforeDay)


            val newCheck = if(markCheckedWhenIsEmpty) {
                if(check==null) true else !check.isChecked
            }else check?.isChecked != true

            if(newCheck){
                if(habit.reminderTimeStamp!=null){
                    NotificationUtils.cancelScheduledNotification(context, habitId = habit.id)
                }
            }



            var ha = habit.toDomain()
            var currentStreak = ha.currentStreak
            if(beforeCheck?.isChecked != true) currentStreak = 0
            var longestStreak = ha.longestStreak
            var longestStreakDayOfYear = ha.longestStreakDayOfYear
            var longestStreakYear = ha.longestStreakYear
            if(newCheck){

                currentStreak++

                if(longestStreak<currentStreak){
                    longestStreak = currentStreak
                    longestStreakDayOfYear = dayOfYear
                    longestStreakYear = year
                }
            }else{
                currentStreak = (currentStreak-1).takeIf { it>-1 }?: 0
                if(longestStreak<currentStreak && (longestStreakDayOfYear == dayOfYear && longestStreakYear == year)){
                    longestStreak = currentStreak
                    longestStreakDayOfYear = dayOfYear
                    longestStreakYear = year
                }else{
                    if(longestStreakDayOfYear == dayOfYear && longestStreakYear == year){
                        longestStreak = currentStreak
                        longestStreakDayOfYear = dayOfYear
                        longestStreakYear = year
                    }else{

                    }
                }
            }


            updateHabit(ha.copy(currentStreak = currentStreak, longestStreak = longestStreak, longestStreakYear = longestStreakYear,
                longestStreakDayOfYear = longestStreakDayOfYear).toEntity())

            delay(50)
            val newHabit = HabitCheck(habitId = habit.id, dayOfYear = dayOfYear, year = year, isChecked = newCheck
            , time = timeFormat.format(Date()).let{
                    DateUtils.getTimeOnlyTimestamp(it.substringBefore(":").toInt(),it.substringAfter(":").toInt())
                }, note = addHabitCheckNote?.trim()?.takeIf { it!="" }?: "")
            check?.id?.let{newHabit.id = it}
            habitCheckRepository.insertCheck(newHabit)
            if(All!=null) loadHabits(loadHabitChecks = loadHabitChecks,All = All)
            if(loadHabitChecksByDayOfYearAndYear) {


                if(newCheck){

                    habitNotCheckChecksByDayOfYearAndYear[habit.id]?.let {
                        val list = it.toMutableList()
                        list.removeIf { it.first == dayOfYear && it.second == year }
                        habitNotCheckChecksByDayOfYearAndYear[habit.id] = list
                    }

                    habitChecksByDayOfYearAndYear[habit.id].let{
                        val newL = it?.toMutableList()?: mutableListOf()


                        habitChecksByDayOfYearAndYear[habit.id] = newL.let {it->
                            val befoRe = it.filter{it.habitId == habit.id && it.dayOfYear == dayOfYear && it.year == year}
                            val index = befoRe.firstOrNull()?.let{i->it.indexOf(i)}

                            if(index!=null) {
                                it[index] = newHabit.copy(isChecked = newCheck)
                            }else{
                                it.add(newHabit)
                            }

                            it

                        }

                    }
                }else{
                    habitNotCheckChecksByDayOfYearAndYear[habit.id].let {
                        val list = it?.toMutableList()?: mutableListOf()
                        list.add(dayOfYear to year)
                        habitNotCheckChecksByDayOfYearAndYear[habit.id] = list
                    }
                }


            }


        }
    }


    // Alışkanlık güncelleme
    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
            loadHabits()
        }
    }


    private val _paginatedHabits = MutableStateFlow<List<HabitEntity>>(emptyList())
    private val _paginatedHabitsForToday = MutableStateFlow<List<HabitEntity>>(emptyList())
    val paginatedHabits: StateFlow<List<HabitEntity>> = _paginatedHabits
    val paginatedHabitsForToday: StateFlow<List<HabitEntity>> = _paginatedHabitsForToday

    val _currentPage = MutableStateFlow(0) // Başlangıç sayfası
    var currentPage: StateFlow<Int> = _currentPage

    private val _totalCount = MutableStateFlow(0) // Toplam kayıt sayısı
    val totalCount: StateFlow<Int> = _totalCount

    var filter = MutableStateFlow<String?>(null)

    val _isActive = MutableStateFlow(true)

    // Sayfa değiştirici
    fun nextPage(loadHabitChecks: Boolean = false,All:Boolean = false) {
        _currentPage.value++
        loadHabits(loadHabitChecks = loadHabitChecks, All=All)
    }

    fun previousPage(loadHabitChecks: Boolean = false,All:Boolean = false) {
        if (_currentPage.value > 0) {
            _currentPage.value--
            loadHabits(loadHabitChecks=loadHabitChecks,All=All)
        }
    }

    val pageSize = 40


    // Verileri yükle
    fun loadHabits(loadHabitChecks: Boolean = false, getTotalCount: Boolean = true, All: Boolean = true
            ,loadHabitChecksByDayOfYear: List<Pair<Int, Int>>?=null ,timeStamp:Long?=null) {
        viewModelScope.launch {





            val habits = habitRepository.getHabitsPaginated(_currentPage.value, pageSize, isActive = _isActive.value, getAll = All
            ,timeStamp = timeStamp)


            val newLi = habits.filter {
                if(filter.value == "show is not completed"){
                    habitChecks.filter { f->f.key == it.id }.get(it.id)?.isChecked != true
                } else if(filter.value == "all"){
                    true
                }else true
            }



            // Toplam kayıt sayısını güncelle
            if(getTotalCount) _totalCount.value = habitRepository.getHabitCount()
            else if (_totalCount.value == 0) {
                _totalCount.value = habitRepository.getHabitCount()
            }

            withContext(Dispatchers.Main) {
                if (All) {
                    _paginatedHabits.value = newLi.sortedBy { !it.isImportant }
                } else {
                    _paginatedHabitsForToday.value = newLi.sortedBy { !it.isImportant }
                }

                if(loadHabitChecksByDayOfYear!=null){

                    isLoadedHabitChecksByDayOfYear = false
                    loadHabitChecksByDayOfYear.forEach {
                        loadHabitChecks(it.first,it.second,habits.map { it.id })
                    }
                    isLoadedHabitChecksByDayOfYear= true
                    isLoadedHabitChecksByDayOfYear = null

                }else if (loadHabitChecks) loadHabitChecks()
            }



        }
    }

    var isLoadedHabitChecksByDayOfYear : Boolean?=null

    fun loadHabitChecks(){
        viewModelScope.launch {
            if(_paginatedHabits.value.isEmpty()){
                loadHabits()
            }else{
                val (dayOfYear, year) = DateUtils.getDayOfYearAndDaYear()
                val checks = habitCheckRepository.getTodayChecksForHabits(paginatedHabits.value.map { it.id }.let{l1->

                    paginatedHabitsForToday.value.map { it.id }.let{l2->
                        (l1 + l2).toSet().toList()
                    }

                },dayOfYear, year)

                checks.forEach { habitChecks[it.habitId] = it }

            }

        }
    }

    fun loadHabitChecks(dayOfYear: Int, year: Int, habitIds: List<Int>){
        viewModelScope.launch {
            if(habitIds.isEmpty()){
            }else{
                val checks = habitCheckRepository.getTodayChecksForHabits(habitIds.let{l1->

                    paginatedHabitsForToday.value.map { it.id }.let{l2->
                        (l1 + l2).toSet().toList()
                    }

                },dayOfYear, year)


                habitIds.forEach { habitId->
                    val before = habitChecksByDayOfYearAndYear[habitId]?.toMutableList()?: mutableListOf()
                    before.addAll( checks.filter { it.habitId == habitId })
                    habitChecksByDayOfYearAndYear[habitId] =before


                    val lis = habitNotCheckChecksByDayOfYearAndYear[habitId]?.toMutableList()?: mutableListOf()
                    checks.filter { it.habitId == habitId && !it.isChecked }.forEach {
                        lis.add(it.dayOfYear to it.year)
                    }

                }


            }

        }
    }



    suspend fun getTodayTotalProgress(daysOfWeek: String, daysOfMonth: String, dayOfYear: Int, year: Int): CountsResult {
        return habitCheckRepository.getTodayProgress(daysOfWeek,daysOfMonth,dayOfYear,year)
    }

}
