package com.taner.taskly.core.utils

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.constraintlayout.helper.widget.Carousel
import androidx.room.Room
import com.taner.taskly.MainActivity.Companion.dailyRememberNotificationID
import com.taner.taskly.MainActivity.Companion.kac_saatte_bir_arkaplan_servisi_bildirimler_icin
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.MainActivity.Companion.reminderAlarmManagerId
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.local.entity.NotificationEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.data.repository.TaskRepository
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.receiver.MidnightReceiver
import com.taner.taskly.presentation.receiver.NotificationReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class NotificationUtils {

    suspend fun postNotification(context: Context, task: Task, delayTime: Int?=null, notEntity: NotificationEntity?=null,
                                 nowTaskReminder: Boolean = true, pastTaskReminder: Boolean = true, ){



        if(delayTime!=null){
            scheduleTaskNotificationTimeStamp(context,task,System.currentTimeMillis() + delayTime
                ,null,0L)
        }else if(nowTaskReminder || pastTaskReminder){

            val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
            val dao = db.notificationDao()

            val notEntity = notEntity?: dao.getNotificationByTaskId(task.id)


            notEntity?.let{

                val nowCalendar = Calendar.getInstance().apply {
                    System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                }

                val nowDayOfWeek = if (nowCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7 else nowCalendar.get(Calendar.DAY_OF_WEEK) - 1
                val nowDayOfMonth = nowCalendar.get(Calendar.DAY_OF_MONTH)
                val nowDayOfYear = nowCalendar.get(Calendar.DAY_OF_YEAR)
                val nowYear = nowCalendar.get(Calendar.YEAR)
                val nowMin = nowCalendar.get(Calendar.MINUTE)
                val nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY)

                val lastShowinDayOfYear = notEntity.lastShownDayOfYear
                val lastShowinYear = notEntity.lastShownYear
                val lastShowinHour = notEntity.lastShownHour
                val lastShowinMinute = notEntity.lastShownMinute

                val entityDaysOfWeek = notEntity.daysOfWeek?.split("<>")?.map { it.toInt() }
                val entityDaysOfMonth = notEntity.dayOfMonth?.split("<>")?.map { it.toInt() }
                val entityDayOfYear = notEntity.dayOfYear
                val entityYear = notEntity.year
                val entityHour = notEntity.hour
                val entityMin = notEntity.minute

                fun isNotShownToday(): Boolean {

                    val isToday = (
                            ((nowDayOfWeek in (entityDaysOfWeek?: emptyList()) ||
                                    nowDayOfMonth in (entityDaysOfMonth?: emptyList())) ||
                                    ((entityDaysOfWeek==null && entityDaysOfMonth == null) &&
                                            (entityDayOfYear == nowDayOfYear && entityYear == nowYear)
                                            )
                                    )
                            ) && (
                            (lastShowinDayOfYear == null && lastShowinYear == null) ||

                                    (lastShowinYear != nowYear || lastShowinDayOfYear != nowDayOfYear)


                            )
                    return isToday
                }


                val isPast = System.currentTimeMillis() > Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, notEntity.hour)
                    set(Calendar.MINUTE, notEntity.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis//notEntity.hour < nowHour || (notEntity.hour == nowHour && notEntity.minute < nowMin)

                if(isNotShownToday() || ((notEntity.daysOfWeek!=null || notEntity.dayOfMonth!=null) && isPast )) {



                    val isCompletedTask = task.isCompleted
                    val isProgressInTask = task.status == TaskStatus.IN_PROGRESS
                    val isNotStartedTask = !task.isCompleted && !isProgressInTask

                    if(isPast && pastTaskReminder){
                        //geçmiş bildiri at
                        val title = "Geçmiş " + if(isProgressInTask){
                            "Devam eden Görev"
                        } else "Başlanmamış Görev"



                        scheduleTaskNotificationTimeStamp(context,task,System.currentTimeMillis() + 30 * 1000L,title,15 * 1000L)

                    }else if(nowTaskReminder){
                        //gelecek


                        val notificationTitle = listOf(
                            "Görev Zamanı Geldi!",
                            "Görev Başlamak Üzere!",
                            "Yeni Görev: Unutmayın!",
                            "Görevinizi Yapma Zamanı!",
                            "Göreviniz Başlıyor!",
                            "Başlamak Üzere: Görev Var!",
                            "Hedefe Adım Adım!",
                            "Göreviniz Başlamak Üzere!",
                            "Yeni Görev İçin Hazır Olun!",
                            "Görevinizi Tamamlama Zamanı!",
                            "Unutmayın, Görev Bekliyor!",
                            "Görev Alarmı: Hazır Mısınız?",
                            "Zaman Geldi: Görevinizi Yapın!",
                            "Yeni Hedef İçin Harekete Geç!",
                            "Görev Uyarısı: Başlayın!",
                            "Göreviniz Sizi Bekliyor!",
                            "Haydi Başlayalım: Görev Zamanı!",
                            "Planladığınız Görev Zamanında!",
                            "Hedefe Giden Yolda İlk Adım!",
                            "Görev İçin Hazır Olun!",
                            "Başarıya Giden Yolculuk Başlıyor!",
                            "Sıradaki Göreviniz Hazır!",
                            "Harekete Geçme Vakti!",
                            "Zamanı Geldi: Görev Seni Bekliyor!",
                            "Kendine Verdiğin Sözü Hatırla!",
                            "Görev Kapıda, Hazır Mısın?",
                            "Yeni Bir Adım Atma Zamanı!",
                            "Disiplinin Gücünü Göster!",
                            "Günlük Görev Zamanı!",
                            "Hedefler İçin Şimdi Başla!",
                            "Kendini Geliştirme Zamanı!",
                            "Planlarına Sadık Kal!",
                            "Görevini Erteleme, Şimdi Yap!",
                            "Şimdi Değilse Ne Zaman?",
                            "Küçük Görev, Büyük Etki!",
                            "İlerlemenin Anahtarı: Devam Et!",
                            "Görevini Bitir, Rahatla!",
                            "Odaklan ve Başla!",
                            "Görev Listeni Temizle!",
                            "Sistem Seni Bekliyor!",
                            "Bugünkü Hedefini Unutma!",
                            "Şimdi Görevini Tamamlama Zamanı!",
                            "Görev Hazır, Sıra Sende!",
                            "Başarının Anahtarı Elinde!",
                            "Görevini Yap, Gününü Kurtar!",
                            "Küçük Başlangıçlar, Büyük Sonuçlar!",
                            "Bir Adım Daha, Devam Et!",
                            "Sana Güveniyoruz, Göreve Başla!",
                            "Odaklan, Derin Nefes Al ve Başla!",
                            "Yarını Bekleme, Bugün Başla!",
                            "Görev Başlatılıyor!",
                            "Sen Yaparsın!",
                            "Bugünlük Hedef Seni Bekliyor!",
                            "Görev Vakti!",
                            "Takvime Bak: Şimdi!",
                            "Kendi Rekorunu Kır!",
                            "Hazırsan, Hemen Başlayalım!",
                            "Disiplin Zamanı!",
                            "Görev Aktif Edildi!"
                        ).random()



                        scheduleTaskNotificationTimeStamp(context,task,nowCalendar.apply {
                            set(Calendar.HOUR_OF_DAY,notEntity.hour)
                            set(Calendar.MINUTE,notEntity.minute)
                        }.timeInMillis,notificationTitle)

                    }


                    val ccc = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY,0)
                        set(Calendar.MINUTE,0)
                        set(Calendar.SECOND,0)
                        add(Calendar.DAY_OF_YEAR,1)
                    }

                    /*dao.updateNotification(notEntity.copy(
                        snoozeMinutes = if(delayTime!=null) (
                                (notEntity.snoozeMinutes?: 0).plus((delayTime?.div(1000L)?.div(60L))?.toInt()?: 0)
                        )else null , isShown = true, year = ccc.get(Calendar.YEAR), dayOfYear = ccc.get(Calendar.DAY_OF_YEAR)
                    ))*/

                    dao.updateNotification(
                        notEntity.copy(
                            lastShownDayOfYear = nowDayOfYear
                            , lastShownYear = nowYear,
                            lastShownMinute = nowMin,
                            lastShownHour = nowHour,
                        )
                    )

                }

                /*   withContext(Dispatchers.Main){
                       Toast.makeText(context, "$isPast ${isNotShownToday() || ((notEntity.daysOfWeek!=null || notEntity.dayOfMonth!=null) && isPast )}", Toast.LENGTH_SHORT).show()

                   }*/

            }

        }


    }


    companion object{


        fun addNotificationToDatabase(context: Context, task: Task){

            var nowTaskReminder = true

            context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {
                nowTaskReminder = getBoolean("nowTaskReminder",true)
            }

            if(nowTaskReminder){
                val nowCalendar = Calendar.getInstance().apply {
                    timeInMillis=System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                }

                val calendar = Calendar.getInstance().apply {
                    if(task.repetition == TaskRepetition.NONE){
                        timeInMillis = task.date?: nowCalendar.timeInMillis
                        set(Calendar.MINUTE, task.time?.substringAfter(":")?.toInt()?: 0)
                        set(Calendar.HOUR_OF_DAY, task.time?.substringBefore(":")?.toInt()?: 0)
                        set(Calendar.SECOND,0)
                    }else {
                        timeInMillis = nowCalendar.timeInMillis
                    }
                }

                val notEntity =
                    NotificationEntity(
                        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) ,
                        month = calendar.get(Calendar.MONTH) +1 ,
                        year = calendar.get(Calendar.YEAR) ,
                        hour = task.time?.substringBefore(":")?.toInt()?: 0,
                        minute = task.time?.substringAfter(":")?.toInt()?: 0,
                        lastShownDayOfYear = null,
                        lastShownYear = null,
                        lastShownHour = null,
                        lastShownMinute = null,
                        taskId = task.id,
                        taskTitle = task.title,
                        taskDescription = task.description?: "",
                        daysOfWeek = (if(TaskRepetition.NONE == task.repetition) null else{
                            if(task.repetition == TaskRepetition.WEEKLY){
                                task.days?.split("||")?.map { daysOfWeek.indexOf(it) + 1 }
                            }else if(task.repetition == TaskRepetition.MONTHLY){
                                null
                            }else{
                                (1..7).toList()
                            }
                        })?.joinToString("<>"),
                        dayOfMonth = (if(TaskRepetition.NONE == task.repetition) null else{
                            if(task.repetition == TaskRepetition.WEEKLY){
                                null
                            }else if(task.repetition == TaskRepetition.MONTHLY){
                                task.days?.split("||")?.map { it.replace("->","").toInt() + 1 }
                            }else{
                                null
                            }
                        })?.joinToString("<>")
                    )

                //val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                // val dao = db.notificationDao()




                CoroutineScope(Dispatchers.IO).launch {


                    /*
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "${notEntity.dayOfMonth} ${notEntity.daysOfWeek}", Toast.LENGTH_SHORT).show()

                    }*/

                    //dao.deleteNotificationsByTaskId(task.id)
                    delay(2000)
                    //dao.insertNotification(notEntity )
                    notViewModel?.addNotification(notEntity)
                    NotificationUtils().postNotification(context, task, notEntity=notEntity)
                }
            }



        }


        fun enableNotification(sp:SharedPreferences, dailyReminderTime: String = "08:00", offsetMin:Int = 15){
            sp.edit().putString("dailyReminderTime",dailyReminderTime).apply()
            sp.edit().putInt("reminderOffsetMinutes",offsetMin).apply()
            sp.edit().putBoolean("pastTaskReminder",true).apply()
            sp.edit().putBoolean("habitReminder",true).apply()
            sp.edit().putBoolean("nowTaskReminder",true).apply()
            sp.edit().putBoolean("isNotEnabled",true).apply()
            sp.edit().remove("is_setted_midnight_receiver").apply()
        }

        fun delayNotificationToDatabase(context: Context, id: Int, delayTime: Int){

            CoroutineScope(Dispatchers.IO).launch {

                val db = Room.databaseBuilder(context, AppDatabase::class.java, "task_db").build()
                val dao = db.taskDao()
                val task = dao.getTaskById(id)

                task?.let{
                    NotificationUtils().postNotification(context,it.toDomain(),delayTime )
                }

            }

        }

        fun scheduleTaskNotificationTimeStamp(context: Context, task: Task, timeStamp: Long, contentTitle: String?=null
        ,notificationOffsetMillis:Long ?=null , useDelayedTitle: Boolean = true){

            var isNotificationMute = true

            var delayTime = 60
            var reminderOffsetMinutes = 15
            context.getSharedPreferences(context.packageName,MODE_PRIVATE).apply {
                delayTime = getInt("delayTime",delayTime)
                reminderOffsetMinutes = getInt("reminderOffsetMinutes",-1)
                isNotificationMute = getBoolean("isNotificationMute",true)
            }


            
            if(reminderOffsetMinutes==-1) return

            val delayedNotificationTitles = listOf(
                "Erteleme Sonrası Görev Zamanı!",
                "Görev Ertelendi, Şimdi Başlayın!",
                "Görev Zamanı Geldi: Ertelendikten Sonra!",
                "Ertelenen Görev Başlamak Üzere!",
                "Görevinizi Ertelediniz, Şimdi Başlayın!",
                "Ertelemeden Sonra: Görev Zamanı!",
                "Ertelenmiş Görev İçin Harekete Geçin!",
                "Erteleme Sonrası Başlama Zamanı!",
                "Ertelenen Görev Başlamak Üzere!",
                "Ertelemeden Sonra Göreviniz Başlıyor!",
                "Şimdi Zamanı Geldi: Ertelenen Görev!",
                "Daha Fazla Erteleme! Hadi Başlayın!",
                "Görevinizi Bitirmenin Tam Zamanı!",
                "Artık Ertelemek Yok!",
                "Erteleme Süresi Doldu!",
                "Hazırsanız Başlıyoruz!",
                "Zaman Doldu, Görev Başlasın!",
                "Ertelemenin Sonu: Harekete Geç!",
                "Bekleme Bitti, Hedefe Odaklan!",
                "Şimdi Tam Zamanı: Ertelenen Görev Seni Bekliyor!",
                "Hedefine Ulaşmak İçin Şimdi Başla!",
                "Kendine Verdiğin Söz Şimdi Başlıyor!",
                "Gecikme Bitti, Eylem Zamanı!",
                "Zamanı Geldi, Artık Harekete Geç!",
                "Planın Seni Bekliyor!",
                "Hedefin Uzakta Değil, Başla!",
                "Şimdi Tam Sırası!",
                "Kaldığın Yerden Devam Etme Zamanı!",
                "Motivasyonun Geri Geldi, Başla!",
                "Yeni Başlangıç İçin Mükemmel Zaman!",
                "Görevinle Yüzleşme Vakti!",
                "Küçük Bir Adım, Büyük Bir Değişim!",
                "Şimdi Olmazsa Ne Zaman?",
                "Eylem Zamanı, Beklemek Yok!",
                "Ertelemek Kolaydı, Başlamak Daha Güzel!",
                "Görev Seni Bekliyor, Harekete Geç!",
                "Planların Gerçek Olsun!",
                "Başarıya Doğru İlk Adımı At!",
                "Kendin İçin Şimdi Başla!",
                "Hedefler Yolda, Yürümeye Devam!"
            )



            var notificationOffsetMillis = notificationOffsetMillis?:reminderOffsetMinutes * 60 * 1000L
            var contentTitle = contentTitle.let{
                if(it!=null) it else {
                    if(useDelayedTitle) delayedNotificationTitles.random() else ""
                }
            }

            fun error(){
                Toast.makeText(context, "bildirim oluşturulamadı", Toast.LENGTH_SHORT).show()

            }


        if (timeStamp == 0L) {
            error()

            return
        }



            val calendar = Calendar.getInstance().apply {

                timeInMillis = timeStamp


            }

            if (calendar.timeInMillis < System.currentTimeMillis()) {


                return
            }


            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("taskId", task.id)
                putExtra("taskTitle", task.title)
                putExtra("notificationTimestamp", calendar.timeInMillis.toString())
                putExtra("delayTime", delayTime.toString())
                putExtra("isNotificationMute", isNotificationMute)
                putExtra("contentTitle", contentTitle)
                putExtra("priority", task.priority.ordinal)
                putExtra("taskDescription", task.description ?: "")
            }


            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis - (notificationOffsetMillis),
                    pendingIntent
                )


            /*    val db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()

                val taskRepository = TaskRepository(taskDao)

                CoroutineScope(Dispatchers.IO).launch {


                    taskRepository.updateTask(task.copy(lastNotificationTime = calendar.timeInMillis
                    , notificationDelayMin = null).toEntity())

                }*/

            } else {

                Toast.makeText(context, "bildirim oluşturulamadı", Toast.LENGTH_SHORT).show()

            }

        }

        fun setReminderAlarm(context: Context){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val HOURS_IN_MILLIS = kac_saatte_bir_arkaplan_servisi_bildirimler_icin * 60 * 60 * 1000L


            val intent = Intent(context, MidnightReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderAlarmManagerId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() + HOURS_IN_MILLIS
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            context.getSharedPreferences(context.packageName,MODE_PRIVATE).apply {
                edit().putLong("is_setted_midnight_receiver",System.currentTimeMillis()).apply()
            }

        }


        fun cancelScheduledNotification(context: Context, taskId: Int?=null, habitId: Int?=null) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                if(taskId!=null) putExtra("taskId", taskId) else putExtra("habitId", habitId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId?: habitId!!,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }


        fun dailyRemember(context: Context, time: String ){


            fun error(){
                Toast.makeText(context, "bildirim oluşturulamadı", Toast.LENGTH_SHORT).show()
            }


            val calendar = Calendar.getInstance().apply {

                timeInMillis = System.currentTimeMillis()

                add(Calendar.DAY_OF_YEAR,1)
                set(Calendar.HOUR_OF_DAY,time.split(":").first().toInt())
                set(Calendar.MINUTE,time.split(":")[1].toInt())
                set(Calendar.SECOND,0)
                set(Calendar.MILLISECOND,0)


            }


            var isNotificationMute = true

            var dailyReminderTime:String? = time
            context.getSharedPreferences(context.packageName,MODE_PRIVATE).apply {
                dailyReminderTime = getString("dailyReminderTime",time)


                isNotificationMute = getBoolean("isNotificationMute",true)

                val day = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }.get(Calendar.DAY_OF_YEAR)
                edit().putInt("lastCallDailyReminderTime",day).apply()
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("daily_remember", true)
                dailyReminderTime?.let{
                    putExtra("dailyReminderTime", dailyReminderTime)
                }
                putExtra("isNotificationMute",isNotificationMute)
            }



            val pendingIntent = PendingIntent.getBroadcast(
                context,
                dailyRememberNotificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

            } else {

                Toast.makeText(context, "bildirim oluşturulamadı", Toast.LENGTH_SHORT).show()

            }

        }

    }
}