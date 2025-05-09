package com.taner.taskly.presentation.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.taner.taskly.MainActivity
import com.taner.taskly.MainActivity.Companion.channelId
import com.taner.taskly.MainActivity.Companion.channelName
import com.taner.taskly.MainActivity.Companion.dailyRememberNotificationChannelId
import com.taner.taskly.MainActivity.Companion.dailyRememberNotificationID
import com.taner.taskly.MainActivity.Companion.importance
import com.taner.taskly.R
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.domain.model.TaskPriority

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val daily_remember = intent.getBooleanExtra("daily_remember",false)
        val habitId = intent.getIntExtra("habitId",-1)
        val habitName = intent.getStringExtra("habitName")

        if(habitId!=-1){

            val habitName = intent.getStringExtra("habitName") ?: "Alışkanlık"
            val notificationTitle = intent.getStringExtra("notificationTitle")
            val notificationContent = intent.getStringExtra("notificationContent")
            val habitContent = intent.getStringExtra("habitContent") ?: "Alışkanlığını gerçekleştir!"
            val isNotificationMute = intent.getBooleanExtra("isNotificationMute", true)


            val clickIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("habitId", habitId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val clickPendingIntent = PendingIntent.getActivity(
                context,
                habitId,
                clickIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            var channelId = "habit_channel_$habitId"
            val channelName = "Habit Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = "Channel for habit reminder notifications"
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
            val completeHabitIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "COMPLETE_HABIT"
                putExtra("habitId", habitId)
            }
            val completeHabitPendingIntent = PendingIntent.getBroadcast(
                context,
                habitId,
                completeHabitIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notificationTitle?: "✅ Alışkanlık Zamanı!")
                .setContentText(notificationContent?: "$habitName - $habitContent")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(clickPendingIntent)
                .addAction(R.drawable.completed, "Yaptım!", completeHabitPendingIntent)
                .setAutoCancel(true)
                .setColor(Color(if(notificationTitle!=null) 0xFFFF5722 else 0xFF3F51B5).toArgb()) // Mavi ton

            if (isNotificationMute) {
                builder.setSilent(true)
            } else {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
            val notification = builder.build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(habitId, notification)

        }else{
            if(!daily_remember){
                val priorityExtra = intent.getIntExtra("priority",TaskPriority.MEDIUM.ordinal)
                val taskId = intent.getIntExtra("taskId",-1)
                val title = intent.getStringExtra("taskTitle") ?: "Görev"
                val contentTitle = intent.getStringExtra("contentTitle") ?: "Görev seni bekliyor!"
                val description = intent.getStringExtra("taskDescription") ?: ""
                val notificationTimestamp = intent.getStringExtra("notificationTimestamp") ?: ""
                val delayTime = intent.getStringExtra("delayTime") ?: ""
                val isNotificationMute = intent.getBooleanExtra("isNotificationMute",true)

                val clickIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("taskId", taskId) // varsa taskId vs gönder
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }


                val clickPendingIntent = PendingIntent.getActivity(
                    context,
                    taskId,
                    clickIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                var channelId = channelId + "_$taskId"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, channelName, importance).apply {
                        this.description = "Channel for task reminder notifications"
                    }
                    val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                val delayIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "MARK_AS_DELAY"
                    putExtra("taskId", taskId)
                    putExtra("notificationTimestamp", notificationTimestamp)
                    putExtra("delayTime", delayTime)
                }

                val delayPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId,
                    delayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val completedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "MARK_AS_COMPLETED"
                    putExtra("taskId", taskId)
                }

                val completedPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId,
                    completedIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val inProgressIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "MARK_AS_IN_PROGRESS"
                    putExtra("taskId", taskId)
                }

                val inProgressPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId,
                    inProgressIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                val priority = when (TaskPriority.entries.get(priorityExtra)) {
                    TaskPriority.HIGH -> NotificationCompat.PRIORITY_MAX
                    TaskPriority.MEDIUM -> NotificationCompat.PRIORITY_HIGH
                    TaskPriority.LOW -> NotificationCompat.PRIORITY_DEFAULT
                }
                val color = when (TaskPriority.entries.get(priorityExtra)) {
                    TaskPriority.HIGH -> Color.Red
                    TaskPriority.MEDIUM -> Color(0xFFFFA500) // Turuncu
                    TaskPriority.LOW -> Color(0xFF4CAF50) // Yeşil
                }
                val dynamicTitle = when (TaskPriority.entries.get(priorityExtra)) {
                    TaskPriority.HIGH -> "‼️ ÖNEMLİ: ${title}"
                    TaskPriority.MEDIUM -> "🔔 Görev: ${title}"
                    TaskPriority.LOW -> "📌 Küçük Hatırlatma: ${title}"
                }


                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(dynamicTitle)
                    .setContentInfo("Detay: $description")
                    .setContentText("Görev: ${title}")
                    .setColor(color.toArgb())
                    .addAction(R.drawable.delay, "Ertele", delayPendingIntent)
                    .addAction(R.drawable.completed, "Tamamlandı", completedPendingIntent)
                    .addAction(R.drawable.in_progress, "Göreve Başla", inProgressPendingIntent)
                    .setPriority(priority)
                    .setContentIntent(clickPendingIntent)
                    .setAutoCancel(true)


                if (isNotificationMute) {
                    builder.setSilent(true)
                } else {
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }

                val notification = builder.build()
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(taskId, notification)
            }else{


                val dailyReminderTime = intent.getStringExtra("dailyReminderTime")
                val isNotificationMute = intent.getBooleanExtra("isNotificationMute", true)

                val clickIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val clickPendingIntent = PendingIntent.getActivity(
                    context,
                    dailyRememberNotificationID,
                    clickIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val channelId = dailyRememberNotificationChannelId

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, channelName, importance).apply {
                        this.description = "Channel for task reminder notifications"
                    }
                    val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                val colors = listOf(
                    Color(0xFF4CAF50), Color(0xFFFFC107),
                    Color(0xFF03A9F4), Color(0xFFF44336), Color(0xFF3F51B5)
                )

                val emojiTitleList = listOf("💪", "🧠", "🔥", "✅", "🎯", "🚀", "📈", "🧗", "🏁", "🕒")
                val emojiTextList = listOf("⚡", "🌟", "⏳", "🔔", "📝", "📌", "✨", "💥", "🏆", "🗓️")
                val notificationMessages = listOf(
                    "Yeni Güne Güçlü Başla!" to "Bugün senden sadece bir şey istiyor: Görevlerini tamamla ve kazanan sen ol!",
                    "Yeni Güne Güçlü Başla!" to "Bugün başarıyı elde etmek için yapman gereken tek şey: Görevlerini bitir!",

                    "Haydi Kahraman!" to "Bugün seni bekleyen görevlerle süper gücünü göster!",
                    "Haydi Kahraman!" to "Hedeflerine ulaşmak için bugün süper bir gün! Görevlerini yap ve kazan!",

                    "Bugün Her Şeyi Değiştir!" to "Küçük bir adım, büyük bir değişim başlatabilir. Görevlerini unutma!",
                    "Bugün Her Şeyi Değiştir!" to "Bugün yapacağın her şey, büyük bir değişim yaratacak. Başla!",

                    "Hedeflerine Kilitlen!" to "Odaklan, planla ve harekete geç. Başarı seni bekliyor!",
                    "Hedeflerine Kilitlen!" to "Bugün, başarıya ulaşmak için en iyi fırsatın! Başarıya odaklan!",

                    "Zirveye Bir Adım Daha!" to "Hedefine ulaşmak için bugün de bir şeyler yap!",
                    "Zirveye Bir Adım Daha!" to "Adım adım zirveye yaklaşıyorsun! Bugün bir adım daha at!",

                    "Zaman Kaçmadan Değerlendir!" to "Dakikalar kıymetli. Görevlerine hemen göz at!",
                    "Zaman Kaçmadan Değerlendir!" to "Zamanın hızla geçiyor, her saniye bir fırsat. Hemen başlayalım!",

                    "Senin Günün Bu Gün!" to "Bugün başarıya ulaşmak için harika bir gün!",
                    "Senin Günün Bu Gün!" to "Bugün senin günün, kendini göster ve tüm görevleri başarıyla tamamla!",

                    "Harekete Geçme Zamanı!" to "Görevlerini yap, ilerle, motive kal!",
                    "Harekete Geçme Zamanı!" to "Bugün, harekete geçme zamanı! Görevleri bitir ve günü fethet!",

                    "Daha Güçlü Bir Yarın İçin" to "Bugünün görevleri, yarının temelini oluşturur.",
                    "Daha Güçlü Bir Yarın İçin" to "Yarına güçlü bir başlangıç için, bugün ne yapabilirsin?",

                    "Sadece 5 Dakika Ayır!" to "Bir göreve başla, gerisi kendiliğinden gelir.",
                    "Sadece 5 Dakika Ayır!" to "Zamanın kısıtlı, ama 5 dakikayı ayırarak her şey değişebilir!",

                    "Motivasyon Dalgası Başladı!" to "Kendine inan ve görevlerini şimdi tamamla!",
                    "Motivasyon Dalgası Başladı!" to "Hedefine ulaşmak için motivasyon dalgasına kapıl! Başla!",

                    "Başarıya Giden Yol Buradan Geçiyor" to "Bir görev, bir adım, bir kazanç!",
                    "Başarıya Giden Yol Buradan Geçiyor" to "Her görev seni bir adım daha başarıya yaklaştırıyor!",

                    "Görev Sırası Sende!" to "Hedef listeni aç ve bugünün yıldızı sen ol!",
                    "Görev Sırası Sende!" to "Bugün, en önemli görevi senin. Başla ve zaferini kazan!",

                    "Günü Kurtarmak Elinde!" to "Basit bir görev bile seni yukarı taşır.",
                    "Günü Kurtarmak Elinde!" to "Bugün her şey mümkün! Hedeflerine adım adım yaklaş!",

                    "Disiplin, En Güçlü Silahın" to "Bugün kendine söz ver ve görevlerini bitir!",
                    "Disiplin, En Güçlü Silahın" to "Güçlü ol, disiplinli ol ve bugün başarıya ulaş!",

                    "Zaman Geçiyor, Hedefler Bekliyor!" to "Harekete geç ve kazanan sen ol.",
                    "Zaman Geçiyor, Hedefler Bekliyor!" to "Hedeflerini erteleme, bugün tam zamanı!",

                    "Kahve Hazırsa Başlayabiliriz ☕" to "Görev zamanı! Hedeflerin seni bekliyor.",
                    "Kahve Hazırsa Başlayabiliriz ☕" to "Kahven hazır mı? O zaman hemen hedeflerine odaklan!",

                    "Her Gün Bir Tuğla!" to "Bugünkü görev, geleceğin temeli.",
                    "Her Gün Bir Tuğla!" to "Bugün bir adım at, geleceği şekillendirmeye başla!",

                    "Unutma, Erteleme = Geri Kalmak" to "Şimdi başla, geleceğin fark yaratsın.",
                    "Unutma, Erteleme = Geri Kalmak" to "Bugün erteleme! Zaman hızla geçiyor, harekete geç!",

                    "Odaklan, Yüklen, Bitir!" to "Görev listesi seni bekliyor!",
                    "Odaklan, Yüklen, Bitir!" to "Bugün her görevi başarıyla bitir, hedeflerine yaklaş!",

                    "Kendi En İyini Yen!" to "Bugün dünkünden daha iyi ol!",
                    "Kendi En İyini Yen!" to "Kendini her gün bir adım daha ileriye taşı!",

                    "Bugünlük Zaferini Planla!" to "Görevlerini tamamla, günü kazan!",
                    "Bugünlük Zaferini Planla!" to "Bugün zaferin senin olacak, başla ve tamamla!",

                    "Kazanmak İçin Başla!" to "Bir göreve başlamak, günü fethetmek demek.",
                    "Kazanmak İçin Başla!" to "Başarı, sadece bir adım uzakta! Başlamak için şimdi tam zamanı!",

                    "Yapılacaklar Seni Bekliyor!" to "Sadece 1 görev, sonra zincirleme motivasyon!",
                    "Yapılacaklar Seni Bekliyor!" to "Görevlerini sırayla yap, başarıyı yakala!",

                    "Motivasyon Seviyesi: 🔥" to "Hedefin belli, zaman şimdi!",
                    "Motivasyon Seviyesi: 🔥" to "Bugün seni bekleyen bir sürü görev var, hemen harekete geç!",

                    "Günün Enerjisi Burada!" to "Hadi bakalım, bugün neler başaracaksın?",
                    "Günün Enerjisi Burada!" to "Bugün, enerjini en iyi şekilde kullanmanın tam zamanı!",

                    "Bugün Başarı İçin Tam Zamanı!" to "Kalk ve harekete geç, başarı seni bekliyor!",
                    "Bugün Hedeflerini Belirle!" to "Plan yap, odaklan ve bir adım at!",

                    "Yeni Bir Gün, Yeni Bir Başlangıç!" to "Her yeni gün, başarıya giden bir adımdır!",
                    "Başarı Yolunda Bir Adım Daha!" to "Bugün ilerlemek için mükemmel bir fırsat!",

                    "Hedefin Belli, Şimdi Başla!" to "İleriye doğru bir adım atmak için daha iyi bir zaman yok!",
                    "İlk Adımı At, Geriye Bakma!" to "Bugün başla, geçmişi unut!",

                    "Hayalini Gerçekleştir!" to "Bugün harekete geçerek hayaline bir adım daha yaklaş!",
                    "Başarı Seninle Gelecek!" to "Odaklan, disiplinli ol ve zaferi kazan!",

                    "Bugün Kazanabileceğin Bir Gün!" to "Hedeflerini unutma, senin günün bu gün!",
                    "Her An, Yeni Bir Fırsat!" to "Başarıya giden yol, her an karşına çıkar!",

                    "Bugünün Planı: Başarı!" to "Hedeflere ulaşmanın tam zamanı, hemen başla!",
                    "Kazananlar Bugün Başlar!" to "Bugün kazanan olma fırsatını kaçırma!",

                    "Daha Güçlü Olmak İçin Başla!" to "Gün bugündür, güçlü ol ve hedeflerine yönel!",
                    "Hayalini Gerçekleştirmek İçin Bugün Başla!" to "Kendi zaferini yaratmak için ilk adım burada!",

                    "Bugün Adım At, Yarın Farkı Gör!" to "Başarı, her adımda gizli! Hedeflerine yaklaş!",
                    "Kendini Zorlama Zamanı!" to "Sınırlarını aş, bugün büyük bir adım at!",

                    "Kendi Potansiyelini Keşfet!" to "İçindeki gücü keşfet ve harekete geç!",
                    "Günü Kazanmak İçin Hedef Belirle!" to "Bugün için küçük bir hedef, büyük bir başarı yaratacak!",

                    "Zorluklar Bizi Güçlendirir!" to "Her zorluk bir fırsattır. Bugün zorlukların üstesinden gel!",
                    "Zamanı Boşa Harcama!" to "Her dakikayı değerlendir, başarı seni bekliyor!",

                    "İlerlemenin Başlangıcı!" to "İlk adımını at, ilerleme başlasın!",
                    "Başarı İçin Disiplin Şart!" to "Disiplinli ol, başarı seni bulur!",

                    "Kendini En İyi Hâlinde Gör!" to "Bugün, en iyi versiyonun olma zamanı!",
                    "Bugün Herşey Senin Elinde!" to "Kontrol sende, hedeflerine ulaşmak için şimdi başla!",

                    "Gün Bugün Senin!" to "Bugün başarıya ulaşmak için bir fırsat daha!",
                    "Başarı İçin Hedeflerine Odaklan!" to "Plan yap, harekete geç, başarı senin olacak!",

                    "Yarının Başarısı Bugünden Başlar!" to "Bugün başla, yarın başarılı ol!",
                    "Kendi Hikayenin Kahramanı Ol!" to "Bugün kendi zaferini yaratmak için ilk adımı at!",

                    "İleriye Doğru Adım At!" to "Bir adım at, büyük farklar yarat!",
                    "Zamanı En İyi Şekilde Değerlendir!" to "Zaman geçiyor, fırsatlar kaçıyor, hemen başlayalım!"
                )



                val (title, text) = notificationMessages.random()
                val emojiTitle = "${emojiTitleList.random()} $title"
                val emojiText = "${emojiTextList.random()} $text"

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(emojiTitle)
                    .setContentText(emojiText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(emojiText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(colors.random().toArgb())
                    .setContentIntent(clickPendingIntent)
                    .setAutoCancel(true)

                if (isNotificationMute) {
                    builder.setSilent(true)
                } else {
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }

                val notification = builder.build()
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(dailyRememberNotificationID, notification)

                dailyReminderTime?.let {
                    NotificationUtils.dailyRemember(context, dailyReminderTime)
                }




            }

        }


    }
}
