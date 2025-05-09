package com.taner.taskly.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.local.dao.HabitCheckDao
import com.taner.taskly.data.local.dao.HabitDao
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.presentation.receiver.NotificationReceiver
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfMonth
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfWeek
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.getNotificationThreshold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Year
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

class HabitRepository(private val habitDao: HabitDao, val habitCheckDao: HabitCheckDao) {

    // Alışkanlık ekleme
    suspend fun addHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }
    suspend fun delHabit(habit: HabitEntity) {
        habitDao.delHabit(habit)
    }
    suspend fun getLastHabit(): HabitEntity? {
        return habitDao.getLastHabit()
    }
    // Alışkanlık güncelleme
    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    // Belirli bir frekansa göre alışkanlıkları alma
    suspend fun getHabitsByFrequency(frequency: HabitFrequency): List<HabitEntity> {
        return habitDao.getHabitsByFrequency(frequency)
    }

    suspend fun getHabitsByTodayNotifications(dayOfWeek: String, dayOfMonth: String): List<HabitEntity> {
        return habitDao.getHabitsByTodayNotifications(dayOfWeek, dayOfMonth)
    }

    // Alışkanlık ID'sine göre alışkanlık alma
    suspend fun getHabitById(id: Int): HabitEntity? {
        return habitDao.getHabitById(id)
    }



    suspend fun getHabitsPaginated(page: Int, pageSize: Int,isActive: Boolean, getAll: Boolean = false,timeStamp:Long?=null): List<HabitEntity> {
        val offset = page * pageSize


        var monthDay: Int?=null
        var weekDay: Int?=null
        if(timeStamp!=null){
            val cal = Calendar.getInstance().apply {
                timeInMillis = timeStamp
            }

            monthDay = cal.get(Calendar.DAY_OF_MONTH)
            weekDay = cal.get(Calendar.DAY_OF_WEEK.let {
                if(it==1) 7 else it-1
            })

        }



        return if(getAll)
            (if(isActive) {
                if(monthDay==null){
                    habitDao.getAllActiveHabitsPaginated(pageSize, offset)

                }else{
                    habitDao.getAllActiveHabitsPaginated(pageSize, offset,dayOfWeek="-$weekDay-",dayOfMonth="->$monthDay<-")

                }
            }
            else {
                if(monthDay==null ){

                    habitDao.getAllInActiveHabitsPaginated(pageSize, offset)
                }else{
                    habitDao.getAllInActiveHabitsPaginated(pageSize, offset,dayOfWeek="-$weekDay-",dayOfMonth="->$monthDay<-")

                }
            })
        else (
                (if(isActive) { if(monthDay!=null ){
                    habitDao.getActiveHabitsPaginated(pageSize, offset,"-$weekDay-","->$monthDay<-")

                }else{
                    habitDao.getActiveHabitsPaginated(pageSize, offset,"-$dayOfWeek-","->$dayOfMonth<-")
                }
                }
                else {

                    if(monthDay==null ){
                        habitDao.getInActiveHabitsPaginated(pageSize, offset,"-$dayOfWeek-","->$dayOfMonth<-")

                    }else{
                        habitDao.getInActiveHabitsPaginated(pageSize, offset,"-$weekDay-","->$monthDay<-")
                    }


                })
        )
    }

    suspend fun getHabitCount(): Int {
        return habitDao.getHabitCount()
    }

    suspend fun getLongestStreak(count: Int): List<HabitEntity>? {
        return habitDao.getLongestStreak(count)
    }



    suspend fun runAnalysis(context: Context) {


        val calendar = Calendar.getInstance().apply {
            timeInMillis = Date().time
        }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = (calendar.get(Calendar.YEAR))
        val rawDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = when (rawDayOfWeek) {
            Calendar.SUNDAY -> 7
            else -> rawDayOfWeek - 1
        }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        var habitReminder = true
        var notify_on_missed_habit = true
        var lastCheckedDayOfYear:Int? = null
        var lastCheckedYear:Int? = null

        var isNotificationMute = true
        context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
            habitReminder = getBoolean("habitReminder",true)
            notify_on_missed_habit = getBoolean("notify_on_missed_habit",true)
            isNotificationMute = getBoolean("isNotificationMute",true)
            lastCheckedDayOfYear = getInt("notify_on_missed_habit_control_day_of_year",-1).takeIf { it>-1 }
            lastCheckedYear = getInt("notify_on_missed_habit_control_year",-1).takeIf { it>-1 }
        }



        if(habitReminder){

            val habs = getHabitsByTodayNotifications("-$dayOfWeek-","->$dayOfMonth<-")

            habs.takeIf { it.isNotEmpty() }?.let{


                val checks = habitCheckDao.getTodayChecksForHabits(habs.map { it.id},dayOfYear, year)

                habs.forEach {habit->


                    if(habit.reminderTimeStamp!=null){

                        if(checks.filter { it.habitId == habit.id }.firstOrNull()?.isChecked != true){
                            habit?.let{

                                val (reminderHour, reminderMinute) = DateUtils.getHourAndMinuteFromTimestamp(habit.reminderTimeStamp!!)



                                val today = timeFormat.format(Date())
                                val nowHour = today.substringBefore(":").toInt()
                                val nowMin = today.substringAfter(":").toInt()

                                if(nowHour>reminderHour || (reminderHour==nowHour && nowMin>reminderMinute)){

                                }else{

                                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                                        putExtra("habitId", habit.id)
                                        putExtra("habitName", habit.name)
                                        putExtra("isNotificationMute", isNotificationMute)
                                        habit.explain.takeIf { it!="" }?.let{putExtra("habitContent", it)}
                                    }

                                    val pendingIntent = PendingIntent.getBroadcast(
                                        context,
                                        habit.id,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )

                                    coroutineScope {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (!alarmManager.canScheduleExactAlarms()) {
                                                // Buraya kullanıcıya bilgi göster veya ayarlara yönlendir
                                                Log.e("Notification", "App does not have permission to schedule exact alarms.")
                                            }else{
                                                try {
                                                    alarmManager.setExactAndAllowWhileIdle(
                                                        AlarmManager.RTC_WAKEUP,
                                                        Calendar.getInstance().apply {
                                                            timeInMillis = System.currentTimeMillis()
                                                            calendar.set(Calendar.HOUR_OF_DAY, reminderHour)
                                                            calendar.set(Calendar.MINUTE, reminderMinute)
                                                            calendar.set(Calendar.SECOND, 0)
                                                            calendar.set(Calendar.MILLISECOND, 0)
                                                        }.timeInMillis,
                                                        pendingIntent
                                                    )
                                                } catch (e: SecurityException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }

                                    }

                                }





                            }
                        }

                    }


                }


            }

        }



        fun getDiff(maxDayOfYear: Int, maxYear: Int, dayOfYear: Int, year: Int): Int {
            val calendar1 = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.DAY_OF_YEAR, dayOfYear)
            }

            val calendar2 = Calendar.getInstance().apply {
                set(Calendar.YEAR, maxYear)
                set(Calendar.DAY_OF_YEAR, maxDayOfYear)
            }

            val diffInMillis = calendar2.timeInMillis - calendar1.timeInMillis
            return abs((diffInMillis / (1000 * 60 * 60 * 24)).toInt())
        }




        if(notify_on_missed_habit &&
            (((year == (lastCheckedYear?:year)) && (dayOfYear - (lastCheckedDayOfYear?:(dayOfYear+7))>=7))
                    ||((year > (lastCheckedYear?:0)))
                    )
            ){

            val eachHabitCheck = habitCheckDao.getLast60DaysChecks(dayOfYear,year)
            val habits = habitDao.getHabitByIds(eachHabitCheck.map { it.habitId }.toSet().toList())

            val thisIsReqNot = mutableListOf<Habit>()
            val reqNotThresHoldDatas = HashMap<Int, Pair<Int,Int>>()
            if(habits!=null){



                habits.forEach {
                    val hChecks = eachHabitCheck.filter { c->c.habitId == it.id }.toMutableList()
                    if(hChecks.isNotEmpty()){
                        it.toDomain().let{



                            val threshold = getNotificationThreshold(it.frequency,it.isImportant,it.days)


                            val controlPeriodDays = threshold.controlPeriodDays
                            val maxMissedCount = threshold.maxMissedCount

                            val sortedChecks = hChecks.sortedWith(
                                compareByDescending<HabitCheckEntity> { it.year }
                                    .thenByDescending { it.dayOfYear }
                            )

                            val lastCheck = sortedChecks.firstOrNull()


                            if (lastCheck != null) {
                                val dayDiff = getDiff(dayOfYear, year,
                                    lastCheck.dayOfYear, lastCheck.year,

                                )

                                // Son X gün içinde kaç tanesi yapılmamış?
                                val missedCount = sortedChecks.count {
                                    val diff = getDiff( dayOfYear, year,it.dayOfYear, it.year,)
                                    diff in 0 until controlPeriodDays && !it.isChecked
                                }


                                val shouldNotify = when {
                                    missedCount > maxMissedCount && dayDiff >= controlPeriodDays -> true

                                    // Yaklaşık olarak eşikteyse ve uzun süredir check yapılmamışsa
                                    missedCount in (maxMissedCount - 1)..maxMissedCount && dayDiff >= controlPeriodDays + 3 -> true

                                    // Çok uzun süredir hiç check yoksa (her şey yanlış olabilir)
                                    missedCount <= maxMissedCount && dayDiff >= controlPeriodDays * 2 -> true

                                    else -> false
                                }


                                if (shouldNotify) {
                                    thisIsReqNot.add(it)
                                    reqNotThresHoldDatas[it.id] = controlPeriodDays to missedCount



                                }
                            }



                        }

                    }
                }


                val sortedForNotification = thisIsReqNot
                    .filter { it.isActive }  // Aktif olanları seçelim
                    .sortedWith(
                        compareByDescending<Habit> { it.isImportant }  // Önce önemli alışkanlıkları getirelim
                            .thenByDescending { reqNotThresHoldDatas[it.id]?.second }  // Missed count'a göre sıralayalım
                            .thenBy { reqNotThresHoldDatas[it.id]?.first }  // Sonrasında kontrol süresi (controlPeriodDays) göre sıralayalım
                    ).take(3)




                val format = timeFormat.format(Date())
                val reminderMinute = format.substringAfter(":").toInt()
                val reminderHour = format.substringBefore(":").toInt()

                sortedForNotification.forEach {
                    
                    val thresold = reqNotThresHoldDatas.get(it.id)
                    

                    val notificationTitles = listOf(
                        "Alışkanlığını Unuttun!",
                        "Bir Gün Daha Kaçtı!",
                        "Hedefinden Sapıyorsun!",
                        "Bugün Günü Kaçırdın!",
                        "Daha Fazla Kaçırma!",
                        "Zamanı Yine Kaçırdın!"
                    )

                    val notificationContents = listOf(
                        "Bu hafta ${it.name} alışkanlığını ${thresold?.second?.takeIf { it>0 }?:"birkaç"} gün kaçırdın! Şimdi harekete geçme zamanı!",
                        "Son ${thresold?.second?.takeIf { it>0 }?:"birkaç"} gün içerisinde hedefini kaçırdın. Başarı için her günü değerlendir!",
                        "Hedefinden sapıyorsun, ${it.name} alışkanlığını ${thresold?.second?.takeIf { it>0 }?:"birkaç"} gün yapmadın. Şimdi başla ve devam et!",
                        "Bugün ${it.name} alışkanlığını aksattın! Henüz geç değil, hemen başlayabilirsin.",
                        "${it.name} alışkanlığını kaçırdığın gün sayısı: ${thresold?.second?.takeIf { it>0 }?:"birkaç"}. Daha fazla kaçırma, hedefe doğru devam et!",
                        "Bu hafta ${it.name} alışkanlığını ${thresold?.second?.takeIf { it>0 }?:"birkaç"} kez yapmadın. Kendine bir şans daha ver!"
                    )
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("habitId", it.id)
                        putExtra("habitName", it.name)
                        putExtra("notificationTitle", notificationTitles.random())
                        putExtra("notificationContent", notificationContents.random())
                        putExtra("isNotificationMute", isNotificationMute)
                        it.explain.takeIf { it!="" }?.let{putExtra("habitContent", it)}
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        it.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    coroutineScope {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (!alarmManager.canScheduleExactAlarms()) {
                                // Buraya kullanıcıya bilgi göster veya ayarlara yönlendir
                                Log.e("Notification", "App does not have permission to schedule exact alarms.")
                            }else{
                                try {
                                    alarmManager.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        Calendar.getInstance().apply {
                                            timeInMillis = System.currentTimeMillis()
                                            calendar.set(Calendar.HOUR_OF_DAY, reminderHour)
                                            calendar.set(Calendar.MINUTE, reminderMinute)
                                            calendar.set(Calendar.SECOND, 0)
                                            calendar.set(Calendar.MILLISECOND, 0)
                                            add(Calendar.MINUTE,(0..30).random())
                                        }.timeInMillis,
                                        pendingIntent
                                    )
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    }

                }




            }





            context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
                edit().putInt("notify_on_missed_habit_control_day_of_year",dayOfYear)
                    .putInt("notify_on_missed_habit_control_year",year).apply()
            }

        }

    }







}
