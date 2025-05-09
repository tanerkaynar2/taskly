package com.taner.taskly.core.utils

import android.os.Build
import android.widget.Toast
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskRepetition
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
class DateUtils {

    companion object{

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateFormat2 = SimpleDateFormat("dd MMM yyyy, EEEE", Locale.getDefault())
        val dateFormat3 = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateFormat4 = SimpleDateFormat("dd MMM yyyy HH:mm, EEEE", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val daysOfWeek = listOf(
            "Pazartesi",
            "Salı",
            "Çarşamba",
            "Perşembe",
            "Cuma",
            "Cumartesi",
            "Pazar"
        )
        val monthNames = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")

        fun isTaskSameDay(task: Task, dayOfMonth: Int): Boolean?{
            var isSameDay:Boolean? = null
            if(task.lastCompletedDate!=null){
                val taskLastCompDate = dateFormat3.parse(task.lastCompletedDate)
                val lastCompCalendar = Calendar.getInstance().apply { timeInMillis = taskLastCompDate.time }
                val taskLastDayOfMonth = lastCompCalendar.get(Calendar.DAY_OF_MONTH)

                isSameDay = taskLastDayOfMonth == dayOfMonth
            }
            return isSameDay
        }

        fun getFormattedDate(date: Long): String? {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
            }
            val timeFormat = dateFormat.format(calendar.time)
            return timeFormat
        }
        fun isTimeInRange(hour: Int, minute: Int, startHour: Int, startMin: Int, endHour: Int, endMin: Int): Boolean {
            // Başlangıç ve bitiş zamanlarını dakikaya çevir
            val startTime = startHour * 60 + startMin
            val endTime = endHour * 60 + endMin

            // Şu anki saati dakikaya çevir
            val currentTime = hour * 60 + minute

            // Eğer currentTime, startTime ve endTime arasında ise true döner
            return currentTime in startTime..endTime
        }
        fun getWeeklySummaryText(selectedDays: List<Int>): String? {
            return when {
                selectedDays.size == 7 -> "Her Gün"
                selectedDays.size == 5 && selectedDays.containsAll(listOf(0, 1, 2, 3, 4)) -> "Hafta içi"
                selectedDays.size == 2 && selectedDays.containsAll(listOf(5, 6)) -> "Hafta Sonu"
                selectedDays.isNotEmpty() -> "Gün: ${daysOfWeek.filterIndexed { index, _ -> selectedDays.contains(index) }.joinToString(", ")}"
                else -> null
            }
        }



        fun getTimeOnlyTimestamp(hour: Int, minute: Int): Long {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, 1970)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

        fun getHourAndMinuteFromTimestamp(timestamp: Long): Pair<Int, Int> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            return hour to minute
        }

        fun countMatchingDays(
            startDayOfYear: Int,
            startYear: Int,
            endDayOfYear: Int,
            endYear: Int,
            targetDays: List<Int>
        ): Int {
            var count = 0
            val calendar = Calendar.getInstance()

            var currentYear = startYear
            var currentDayOfYear = startDayOfYear

            while (currentYear < endYear || (currentYear == endYear && currentDayOfYear <= endDayOfYear)) {
                calendar.set(Calendar.YEAR, currentYear)
                calendar.set(Calendar.DAY_OF_YEAR, currentDayOfYear)

                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                if (dayOfMonth in targetDays) count++

                currentDayOfYear++
                if (currentDayOfYear > calendar.getActualMaximum(Calendar.DAY_OF_YEAR)) {
                    currentDayOfYear = 1
                    currentYear++
                }
            }

            return count
        }

        fun countMatchingWeekdays(
            startDayOfYear: Int,
            startYear: Int,
            endDayOfYear: Int,
            endYear: Int,
            targetWeekdays: List<Int> // 1 = Pazartesi, 7 = Pazar
        ): Int {
            var count = 0
            val calendar = Calendar.getInstance()

            var currentYear = startYear
            var currentDayOfYear = startDayOfYear

            while (currentYear < endYear || (currentYear == endYear && currentDayOfYear <= endDayOfYear)) {
                calendar.set(Calendar.YEAR, currentYear)
                calendar.set(Calendar.DAY_OF_YEAR, currentDayOfYear)

                val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar
                if (dayOfWeek in targetWeekdays) count++

                currentDayOfYear++
                if (currentDayOfYear > calendar.getActualMaximum(Calendar.DAY_OF_YEAR)) {
                    currentDayOfYear = 1
                    currentYear++
                }
            }

            return count
        }


        fun getDaysOfMonth(calendar: Calendar): List<Int> {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            return List(maxDay) { it + 1 }
        }


        fun getDaysOfMonthForWeekdays(calendar: Calendar, allowedWeekdays: List<Int>? = null): List<Int> {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val tempCalendar = calendar.clone() as Calendar

            return (1..maxDay).filter { day ->
                tempCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dayOfWeek = (tempCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar
                allowedWeekdays == null || dayOfWeek in allowedWeekdays
            }
        }

        fun getDaysOfMonthForWeekdays(calendar: Calendar, allowedWeekdays: List<Int>? = null, showWeekName: Boolean = false): List<Pair<Int, String>> {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val tempCalendar = calendar.clone() as Calendar




            return (1..maxDay).mapNotNull { day ->
                tempCalendar.set(Calendar.DAY_OF_MONTH, day)
               // val dayOfWeek = (tempCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar

                val dayOfWeek = if (tempCalendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                else tempCalendar.get(Calendar.DAY_OF_WEEK) - 1


                if (allowedWeekdays == null || dayOfWeek in allowedWeekdays) {
                    Pair(day, daysOfWeek[dayOfWeek - 1])
                } else {
                    null
                }
            }.toMutableList().apply {
                add(tempCalendar.get(Calendar.YEAR) to tempCalendar.get(Calendar.MONTH).toString())
            }
        }




        fun getDaysOfMonthForWeekdays2(calendar: Calendar, allowedWeekdays: List<Int>? = null, showWeekName: Boolean = false): List<Pair<Pair<Int, Int>, Pair<Int, String>>> {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val tempCalendar = calendar.clone() as Calendar




            return (1..maxDay).mapNotNull { day ->
                tempCalendar.set(Calendar.DAY_OF_MONTH, day)
               // val dayOfWeek = (tempCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar

                val dayOfWeek = if (tempCalendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                else tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
                val dayOfYear = (tempCalendar.get(Calendar.DAY_OF_YEAR))
                val YEAR = (tempCalendar.get(Calendar.YEAR))


                if (allowedWeekdays == null || dayOfWeek in allowedWeekdays) {
                    (dayOfYear to YEAR) to (Pair(day, daysOfWeek[dayOfWeek - 1]))
                } else {
                    null
                }
            }.toMutableList().apply {
            }
        }


        fun getMonthRange(habitStart: Calendar, today: Calendar, offsetMonth: Int): List<Pair<Int, Int>> {
            val months = mutableListOf<Pair<Int, Int>>()

            // Başlangıç ayını hesapla: today - offsetMonth
            val calendarFrom = today.clone() as Calendar
            calendarFrom.add(Calendar.MONTH, -offsetMonth)

            // Eğer habit daha yeni başladıysa, habitStart'ı kullan
            if (habitStart.after(calendarFrom)) {
                calendarFrom.timeInMillis = habitStart.timeInMillis
            }

            val calendarTo = today.clone() as Calendar

            while (
                calendarFrom.get(Calendar.YEAR) < calendarTo.get(Calendar.YEAR) ||
                (calendarFrom.get(Calendar.YEAR) == calendarTo.get(Calendar.YEAR) &&
                        calendarFrom.get(Calendar.MONTH) <= calendarTo.get(Calendar.MONTH))
            ) {
                months.add(Pair(calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH)))
                calendarFrom.add(Calendar.MONTH, 1)
            }

            return months
        }
        fun getFirstWeekdayOfMonth(weekDay: Int, year: Int, month: Int): Int {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            while (true) {
                val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                else calendar.get(Calendar.DAY_OF_WEEK) - 1

                if (currentDayOfWeek == weekDay) {
                    return calendar.get(Calendar.DAY_OF_MONTH)
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }


        fun getFirstWeekdayOfMonth(weekDay: Int, year: Int, month: Int,startDayOfMonth:Int): Int {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, startDayOfMonth)
            }

            while (true) {
                val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                else calendar.get(Calendar.DAY_OF_WEEK) - 1

                if (currentDayOfWeek == weekDay) {
                    return calendar.get(Calendar.DAY_OF_MONTH)
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }



        fun getDaysOfMonthForWeekdays(startDayOfMonth: Int=1, calendar: Calendar, allowedWeekdays: List<Int>? = null, showWeekName: Boolean = false): List<Pair<Int, String>> {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val tempCalendar = calendar.clone() as Calendar


            return (startDayOfMonth..maxDay).mapNotNull { day ->
                tempCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dayOfWeek = (tempCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar

                if (allowedWeekdays == null || dayOfWeek in allowedWeekdays) {
                    Pair(day, daysOfWeek[dayOfWeek - 1])
                } else {
                    null
                }
            }
        }


        fun getDayOfYearListForMonth(monthIndex: Int, year: Int): List<Int> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (1..daysInMonth).map {
                calendar.set(Calendar.DAY_OF_MONTH, it)
                calendar.get(Calendar.DAY_OF_YEAR)
            }
        }
        fun getDayOfYearListForMonth(
            monthIndex: Int,
            year: Int,
            allowedDays: List<Int>? = null // 1-31 arası gün listesi
        ): List<Int> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (1..daysInMonth).filter { allowedDays == null || it in allowedDays }.map {
                calendar.set(Calendar.DAY_OF_MONTH, it)
                calendar.get(Calendar.DAY_OF_YEAR)
            }
        }


        fun getDayOfYearListForWeekdays(
            monthIndex: Int,
            year: Int,
            allowedWeekdays: List<Int>? = null // 1 = Pazartesi, ..., 7 = Pazar
        ): List<Int> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (1..daysInMonth).mapNotNull { day ->
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar
                if (allowedWeekdays == null || dayOfWeek in allowedWeekdays) {
                    calendar.get(Calendar.DAY_OF_YEAR)
                } else null
            }
        }
        fun getDayOfYearListForWeekdays(
            weekday: Int, // 1 = Pazartesi, ..., 7 = Pazar
            monthIndex: Int,
            year: Int,
            allowedWeekdays: List<Int>? = null // 1 = Pazartesi, ..., 7 = Pazar
        ): List<Int> {
            val targetDay = ((weekday) % 7).let { if (it == 0) 7 else it }
            var dayStart = 1
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
                val targetDay = ((weekday) % 7).let { if (it == 0) 7 else it }
                while (get(Calendar.DAY_OF_WEEK) != targetDay) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                dayStart = get(Calendar.DAY_OF_MONTH)
            }

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (dayStart..daysInMonth).mapNotNull { day ->
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Pazartesi, 7 = Pazar
                if (allowedWeekdays == null || dayOfWeek in allowedWeekdays) {
                    calendar.get(Calendar.DAY_OF_YEAR)
                } else null
            }
        }


        fun getDayOfYearAndDaYear(): Pair<Int, Int> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = Date().time
            }
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val year = (calendar.get(Calendar.YEAR))
            return (dayOfYear to year)
        }

        fun countSpecificWeekDaysSinceCreated(createdAt: Long, targetWeekDayIndices: Set<Int>): Int {
            require(targetWeekDayIndices.all { it in 0..6 }) { "Haftalık gün indeksleri 0-6 arasında olmalı." }

            val startCal = Calendar.getInstance().apply {
                timeInMillis = createdAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var count = 0
            val current = startCal.clone() as Calendar

            while (!current.after(endCal)) {
                val dayIndex = (current.get(Calendar.DAY_OF_WEEK) + 5) % 7  // Java’da Pazar 1, bu düzeltmeyle 0 = Pazartesi
                if (dayIndex in targetWeekDayIndices) count++
                current.add(Calendar.DAY_OF_MONTH, 1)
            }

            return count
        }
        fun countSpecificMonthDaysSinceCreated(start: Long,targetDayOfMonthIndices: Set<Int>, end: Long = System.currentTimeMillis()): Int {
            require(targetDayOfMonthIndices.all { it in 0..30 }) { "Ay gün indeksleri 0-30 arasında olmalı." }

            val startCal = Calendar.getInstance().apply {
                timeInMillis = start
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                timeInMillis = end
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (startCal.after(endCal)) return 0

            var count = 0
            val current = startCal.clone() as Calendar

            while (!current.after(endCal)) {
                val maxDay = current.getActualMaximum(Calendar.DAY_OF_MONTH)
                targetDayOfMonthIndices.forEach { index ->
                    if (index + 1 <= maxDay) count++
                }
                current.add(Calendar.MONTH, 1)
            }

            return count
        }


        fun getRemainingTimeText(
            targetDay: Int?=null, // 1-31
            targetMonth: Int?=null, // 1-12
            targetYear: Int?=null, // 2020
            targetDate: Long?=null, // timestamp
            targetTime: String?=null, // HH:mm
            nowTime: Long?=null,
        ): String {
            val now = Calendar.getInstance().apply { nowTime?.let{timeInMillis = it} }

            val month = now.get(Calendar.MONTH) + 1
            val dayOfMonth = now.get(Calendar.DAY_OF_MONTH) // 1-31 arası
            val year = now.get(Calendar.YEAR)

            // Şu anki saat ve dakika
            val currentTime = Calendar.getInstance().apply { nowTime?.let{timeInMillis = it} }

            var dayText = ""
            var yearText = ""
            var monthText = ""
            var timeText = ""


            val targetDate = targetDate?.let{Calendar.getInstance().apply { timeInMillis = it }}




            var targetDay = targetDate?.get(Calendar.DAY_OF_MONTH) ?: targetDay
            var targetMonth = (targetDate?.get(Calendar.MONTH)?.plus(1)) ?: targetMonth
            var targetYear = (targetDate?.get(Calendar.YEAR)) ?: targetYear



            var isPast:Boolean? = null//false



            if(targetYear!=null){
                (targetYear-year).takeIf { it!=0 }?.let{
                    yearText="$it Yıl "
                    isPast = it<0
                }
            }

            if(targetMonth!=null){
                (targetMonth-month).takeIf { it!=0 }?.let{
                    monthText+="$it Ay "
                    if(isPast!=true) isPast = it<0
                }
            }


            if(targetDay!=null){
                (targetDay-dayOfMonth).takeIf { it!=0 }?.let{
                    dayText+="$it Gün "
                    if(isPast!=true) isPast = it<0
                }
            }

            // 4. Saat varsa
            if (targetTime != null) {
                val nowTime = timeFormat.parse(timeFormat.format(currentTime.time))

                val targetParsedTime = timeFormat.parse(targetTime)
                var timeDiff = targetParsedTime.time - nowTime.time
                if(isPast==false){
                    if(timeDiff<0){
                        timeDiff += 86400000  // 24 saat (ms cinsinden)

                        val year = yearText.replace(" Yıl ","").toIntOrNull()
                        yearText = (year?.minus(1))?.takeIf{it>0}?.let{"$it Yıl "}?: ""
                        val month = monthText.replace(" Ay ","").toIntOrNull()
                        monthText = (month?.minus(1))?.takeIf{it>0}?.let{"$it Ay "}?: ""
                        val day = dayText.replace(" Gün ","").toIntOrNull()
                        dayText = (day?.minus(1))?.takeIf{it>0}?.let{"$it Gün "}?: ""

                    }
                }
                val hours = kotlin.math.abs(TimeUnit.MILLISECONDS.toHours(timeDiff))
                val minutes = kotlin.math.abs(TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60)
                if(isPast==null){
                    isPast = timeDiff < 0
                }
                timeText = "${if(hours>0)"$hours saat " else " "}${if(minutes>0) "$minutes  dk " else " "}"
            }



            return listOf(yearText, monthText, dayText, timeText).filter {
                it.isNotBlank() }.joinToString(" • ") + if (isPast==true) "geçmiş" else if(isPast==false) "kaldı" else ""
        }







        fun getRemainingTimeText2(
            targetDay: Int? = null,
            targetMonth: Int? = null,
            targetYear: Int? = null,
            targetDate: Long? = null,
            targetTime: String? = null, // "HH:mm"
            nowTime: Long? = null
        ): String {
            val zone = ZoneId.systemDefault()
            val now = nowTime?.let { Instant.ofEpochMilli(it).atZone(zone) } ?: ZonedDateTime.now(zone)

            val targetDateTime = when {
                targetDate != null -> {
                    val base = Instant.ofEpochMilli(targetDate).atZone(zone)
                    if (targetTime != null) {
                        val (h, m) = targetTime.split(":").mapNotNull { it.toIntOrNull() }
                        base.withHour(h).withMinute(m)
                    } else base
                }

                targetDay != null && targetMonth != null && targetYear != null -> {
                    try {
                        var base = ZonedDateTime.of(targetYear, targetMonth, targetDay, 0, 0, 0, 0, zone)
                        if (targetTime != null) {
                            val (h, m) = targetTime.split(":").mapNotNull { it.toIntOrNull() }
                            base = base.withHour(h).withMinute(m)
                        }
                        base
                    } catch (e: Exception) {
                        return ""
                    }
                }

                else -> return ""
            }

            val duration = Duration.between(now, targetDateTime)
            val isPast = duration.isNegative
            val diff = duration.abs()

            val totalMinutes = diff.toMinutes()
            val days = totalMinutes / (60 * 24)
            val hours = (totalMinutes % (60 * 24)) / 60
            val minutes = totalMinutes % 60

            // ⛔️ 0 gün 0 saat 0 dakika saçmalığını engelle
            if (days == 0L && hours == 0L && minutes == 0L) return "Şu an"

            val parts = listOfNotNull(
                days.takeIf { it > 0 }?.let { "$it gün" },
                hours.takeIf { it > 0 }?.let { "$it saat" },
                minutes.takeIf { it > 0 }?.let { "$it dk" }
            )

            return parts.joinToString(" • ") + if (isPast) " önce" else " kaldı"
        }




        fun shouldResetTask(task: Task): Boolean {
            val now = Calendar.getInstance()
            val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Pazar, 6 = Cumartesi
            val todayDayOfMonth = now.get(Calendar.DAY_OF_MONTH)


            val lastDate = task.lastCompletedDate?.let {
                dateFormat3.parse(it)
            }?.let {
                Calendar.getInstance().apply { time = it }
            }


            if(lastDate!=null){


                if (task.repetition != TaskRepetition.NONE) {
                    return lastDate?.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR) ||
                            lastDate?.get(Calendar.YEAR) != now.get(Calendar.YEAR)
                }

            }


            return false

        }



    }
}