package com.taner.taskly.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.taner.taskly.core.utils.NotificationUtils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            context.getSharedPreferences(context.packageName, MODE_PRIVATE).apply {


                val lastSetTime = getLong("is_setted_midnight_receiver", 0L)
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastSetTime > 5 * 60 * 60 * 1000) { // 5 saat
                    NotificationUtils.setReminderAlarm(context)
                }


            }


        }
    }
}
