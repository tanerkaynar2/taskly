package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.alpha
import com.taner.taskly.MainActivity
import com.taner.taskly.MainActivity.Companion.isDarkTaskCard
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.getFormattedDate
import com.taner.taskly.core.utils.DateUtils.Companion.getHourAndMinuteFromTimestamp
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.DateUtils.Companion.isTaskSameDay
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun HabitCard(habit: Habit, index: Int, onClick: (Int) -> Unit, habitSuccessRate: suspend (Int)->Int
              , delHabit: (Habit)->Unit, editHabit: (Habit)->Unit
,activeToggleHabit : (Int)->Unit) {

    var showLongClickDialog by remember { mutableStateOf(false) }
    var showDelDialog by remember { mutableStateOf(false) }
    var successRate by remember { mutableStateOf(0) }


    if(showDelDialog){
        AlertDialog(
            onDismissRequest = { showDelDialog = false },
            title = {
                Text(text = "Emin misin?",color = MaterialTheme.colorScheme.onSecondary)
            },
            text = {
                Text("Bu öğeyi silmek istediğine emin misin?",color = MaterialTheme.colorScheme.onSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    delHabit.invoke(habit)



                    showDelDialog = false
                }) {
                    Text("Evet",color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelDialog = false }) {
                    Text("İptal",color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }

    if(showLongClickDialog){
        AlertDialog(
            onDismissRequest = { showLongClickDialog = false },
            title = { Text(habit.name,color = MaterialTheme.colorScheme.onSecondary) },
            text = { Text("",color = MaterialTheme.colorScheme.onSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDelDialog = true

                    showLongClickDialog = false
                }) {
                    Text("Sil",color = Color.Red)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        activeToggleHabit.invoke(habit.id)
                        showLongClickDialog = false
                    }) {
                        Text( if(habit.isActive)"Pasif   " else "Aktif   ",color = MaterialTheme.colorScheme.onSecondary)
                    }
                    TextButton(onClick = {
                        editHabit.invoke(habit)
                        showLongClickDialog = false
                    }) {
                        Text("Düzenle",color = MaterialTheme.colorScheme.onSecondary)
                    }
                }

            }
        )
    }

    val containerColor = (if(isDarkTaskCard){

        if(isDarkTheme()) listOf(Color(0xFF2C2C2C),Color(0xFF1A1A1A)) else listOf(Color(0xFFB0B0B0),Color(0xFF9E9E9E))

} else {
        (listOf(Color(habit.color!! ).copy(
            if(isDarkTheme() || (isLightTheme() && habit.color == Color.White.hashCode())) 0.1f else 1f),

            Color(habit.color!! ).copy(if(isDarkTheme()  || (isLightTheme() && habit.color == Color.White.hashCode())) 0.3f else 1f)/*Color(0xFF1F2633), Color(0xFF332B1F)*//*Color.DarkGray,Color.Gray*/)
                )
}.get(index % 2)).let{
    if(!habit.isActive) it.copy(0.009f) else it
    }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(habit.id) },
                    onLongPress = {


                        showLongClickDialog = true

                    }
                )
            }
            ,colors = CardDefaults.cardColors(
            containerColor = containerColor//task.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
            )
    ) {
/*
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(task.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surface)
        )*/

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {


            Spacer(modifier = Modifier.width(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {

                Column(Modifier.weight(2f).padding(vertical = 4.dp)) {



                    Row(Modifier.fillMaxWidth()){

                        Text(text = habit.name, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if(!habit.isActive) TextDecoration.LineThrough else null
                        ), modifier = Modifier.weight(2f), maxLines = 4,
                            textAlign = TextAlign.Start)




                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                            //Spacer(modifier = Modifier.width(36.dp))
                            PriorityDotCard(null, isImportant = habit.isImportant)
                        }
                    }



                    habit.explain.takeIf { it!="" }?.let {
                        Text(text = it, color = Color(0xFFDDDDDD), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Start, maxLines = 2,)
                    }


                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                        habit.reminderTimeStamp?.let {

                            getHourAndMinuteFromTimestamp(it).let {
                                it.toList().joinToString(":").let{
                                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                        Image(Icons.Default.Notifications,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))
                                        Text(text = "    $it", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
                                    }
                                }
                            }

                        }


                        @Composable
                        fun addDateText(text: String){

                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                Image(Icons.Default.CalendarMonth,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier.padding(end = 4.dp))
                                Text(text = "   $text", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        when(habit.frequency){
                            HabitFrequency.DAILY->{
                                addDateText("Her Gün")
                            }
                            HabitFrequency.WEEKLY->{
                                val days = habit.days
                                days?.let{

                                    addDateText(getWeeklySummaryText(it).takeIf { it!=null && !it.contains("Gün:") }
                                        ?: ("Her Hafta " + it.sorted().map { daysOfWeek.get(it-1) }.joinToString(", ") + " Günleri"))

                                }
                            }
                            HabitFrequency.MONTHLY->{
                                val days = habit.days

                                days?.let{
                                    it.map { "${it}" }.joinToString ( ", " ).let {
                                        addDateText(it)
                                    }
                                }
                            }
                            else->{}
                        }






                    }


                    Row(Modifier.fillMaxWidth().padding( 16.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {



                        Row(Modifier.weight(1f).padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                            Image(Icons.Default.MilitaryTech,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier.padding(end = 4.dp))
                            Text(text = "   En Uzun seri ${habit.longestStreak} Gün",
                                color = if(habit.currentStreak==habit.longestStreak && habit.longestStreak!=0) Color.Green else MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                        }

                        habit.longestStreakDayOfYear?.let{day->
                            habit.longestStreakYear?.let{year->
                                Row(Modifier.weight(1f).padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                    Image(Icons.Default.MilitaryTech,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                        modifier = Modifier.padding(end = 4.dp))
                                    Text(text = " ${Calendar.getInstance().apply { 
                                        set(Calendar.DAY_OF_YEAR,day)
                                        set(Calendar.YEAR,year)
                                    }.let{ dateFormat.format(it.timeInMillis)}} ", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }


                        Row(Modifier.weight(1f).padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                            Image(Icons.Default.Whatshot,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier.padding(end = 4.dp))
                            Text(text = "   Mevcut seri ${habit.currentStreak} Gün",
                                color = if(habit.currentStreak==habit.longestStreak && habit.longestStreak!=0) Color.Green else
                                    MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                        }


                    }


                    }

            }
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TaskLabel(habit.category,habit.color?.let{Color(it)}?: Color.White)
        }


        Box(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {

            LinearProgressIndicator(
                progress = successRate / 100f,
                strokeCap = StrokeCap.Square,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 48.dp),
                color = when {
                    successRate >= 90 -> Color(0xFF4CAF50) // Gold
                    successRate >= 50 -> Color(0xFFFFD700) // Green
                    else -> Color(0xFFF44336)             // Red
                },
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            Text(
                text = "$successRate%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.wrapContentWidth().align(Alignment.CenterEnd).padding(top = 16.dp, end = 16.dp),
                fontSize = 10.sp
            )
        }
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                successRate = habitSuccessRate.invoke(habit.id)
            }
        }

    }
}

@Composable
fun TaskLabel(
    category: String?=null,
    baseColor: Color,
) {

    /*  var statusType =  when (status) {
          "TAMAMLANDI" -> 2
          "Başlamadı" -> 0
          "Devam Ediyor" -> 1
          else -> Icons.Default.Error
      }*/
    /*   val statusIcon = when (statusType) {
           2 -> Icons.Default.CheckCircle
           0,1 -> Icons.Default.RadioButtonUnchecked
           else -> Icons.Default.Error
       }
       val statusColor = when (statusType) {
           2 -> Color.Green
           0 -> Color.Gray
           1 -> Color.Yellow
           else -> Color.Red
       }*/
    if(category!=null && category.trim() != ""){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                // .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kategori
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = "Kategori Icon", tint = baseColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = category.take(50), style = MaterialTheme.typography.bodyMedium, color = baseColor)
            }

        }
    }





}

