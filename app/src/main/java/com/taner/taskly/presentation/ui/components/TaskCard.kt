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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.DateUtils.Companion.isTaskSameDay
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import java.util.Calendar
import java.util.Date

@Composable
fun TaskCard(task: Task, index: Int, onClick: (Int) -> Unit,  onCompleteToggle: () -> Unit
             ,delTask: (Task)->Unit,editTask: (Task)->Unit, dayOffset:Long?=null, dayOfMonth:Int) {

    var showLongClickDialog by remember { mutableStateOf(false) }
    var showDelDialog by remember { mutableStateOf(false) }

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
                    delTask.invoke(task)



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
            title = { Text(task.title,color = MaterialTheme.colorScheme.onSecondary) },
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
                TextButton(onClick = {
                    editTask.invoke(task)
                    showLongClickDialog = false
                }) {
                    Text("Düzenle",color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }

    val containerColor = if(isDarkTaskCard){

        if(isDarkTheme()) listOf(Color(0xFF2C2C2C),Color(0xFF1A1A1A)) else listOf(Color(0xFFB0B0B0),Color(0xFF9E9E9E))

} else {
        (listOf(Color(task.color!! ).copy(
            if(isDarkTheme() || (isLightTheme() && task.color == Color.White.hashCode())) 0.1f else 1f),

            Color(task.color!! ).copy(if(isDarkTheme()  || (isLightTheme() && task.color == Color.White.hashCode())) 0.3f else 1f)/*Color(0xFF1F2633), Color(0xFF332B1F)*//*Color.DarkGray,Color.Gray*/)
                )
}.get(index % 2)

    var targetDayOfMonth = if(task!!.repetition != TaskRepetition.NONE) {
        dayOffset?.takeIf { it>0 }?.let{
            dayOfMonth + it
        }
    } else null

    var isSameDay = dayOffset?.let{it.takeIf { it>0 }?.let { isTaskSameDay(task,dayOfMonth + it.toInt()) }}


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(task.id) },
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

            Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                // Öncelik simgesi

                if(isSameDay!=false){


                    Checkbox(checked = (task.isCompleted), onCheckedChange = { onCompleteToggle() })


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if(task.status == TaskStatus.IN_PROGRESS){
                            Icon(imageVector = Icons.Default.Timelapse,
                                contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(end = 4.dp).size(16.dp))
                        }
                        Text(text = when(task.status){TaskStatus.NOT_STARTED-> "Başlanmadı" ; TaskStatus.IN_PROGRESS -> "Devam ediyor";else->""} ,
                            color = when(task.status){TaskStatus.NOT_STARTED-> MaterialTheme.colorScheme.onSecondary.copy(alpha = if(isDarkTheme()) 0.2f else 1f) ; TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondary;else->MaterialTheme.colorScheme.onSecondary}
                            , fontSize = 14.sp)

                    }
                }


                if(task.status == TaskStatus.COMPLETED || isSameDay == false){
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }


            Spacer(modifier = Modifier.width(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {

                Column(Modifier.weight(2f).padding(vertical = 4.dp)) {

                    Text(text = task.title, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if(task.isCompleted) TextDecoration.LineThrough else null
                    ), modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start)

                    task.description?.let {
                        Text(text = it, color = Color(0xFFDDDDDD), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Start, maxLines = 2,)
                    }


                    Row(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                        task.time?.let {

                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                Image(Icons.Default.AccessTime,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))
                                Text(text = "    $it", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
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

                        when(task.repetition){
                            TaskRepetition.NONE->{
                                val timestamp = task.date
                                timestamp?.let{timestamp->

                                    task.time?.let {
                                        getFormattedDate(timestamp)?.let { it1 -> addDateText(it1) }
                                    }


                                }

                            }
                            TaskRepetition.WEEKLY->{
                                val days = task.days
                                days?.split("||")?.let{

                                    addDateText(getWeeklySummaryText(it.map { daysOfWeek.indexOf(it.trim()) }).takeIf { it!=null && !it.contains("Gün:") }
                                        ?: ("Her Hafta " + it.joinToString(", ") + " Günleri"))



                                }
                            }
                            TaskRepetition.MONTHLY->{
                                val days = task.days

                                days?.split("||")?.map { it.replace("->","").toIntOrNull() }?.filterNotNull()?.let{
                                    it.map { "${it + 1}" }.joinToString ( ", " ).let {
                                        addDateText(it)
                                    }
                                }
                            }
                            else->{}
                        }






                    }


                }


                Row(modifier = Modifier.weight(1f).fillMaxHeight(), verticalAlignment = Alignment.Top) {
                    //Spacer(modifier = Modifier.width(36.dp))
                    PriorityDotCard(task)
                }

            }
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            if(!task.isCompleted){
                val remainingTimeText = DateUtils.getRemainingTimeText(
                    targetDay = targetDayOfMonth?.toInt(),
                    targetMonth = null,
                    targetYear = null,
                    targetDate = task!!.date?.takeIf {(task!!.repetition == TaskRepetition.NONE)},
                    targetTime = task!!.time,
                ).let{

                    if(it.contains("Gün")){

                        val li = it.split(" ")
                        val ix = li.indexOf("Gün")
                        if(ix>0){
                            val dayCount = li.get(ix-1).toIntOrNull()

                            dayCount?.let{dayCount->
                                if(dayCount>25){
                                    ""
                                }else it
                            }?: it
                        }else it

                    }else it

                }
                if(remainingTimeText.trim()!=""){

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if(remainingTimeText.contains("geçmiş")) Icons.Default.WatchLater else Icons.Default.Info,
                            contentDescription = "Bilgi",
                            tint = (if(remainingTimeText.contains("geçmiş")) Color.Red else  MaterialTheme.colorScheme.onSecondary).
                            copy(alpha = if(remainingTimeText.contains("geçmiş")) 1f else 0.8f),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = remainingTimeText.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = (if(remainingTimeText.contains("geçmiş")) Color.Red else MaterialTheme.colorScheme.onSecondary).
                                copy(alpha = if(remainingTimeText.contains("geçmiş")) 1f else 0.85f),
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = if(isDarkTheme()) 0.2f else 1f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 1f
                                )
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }


                }
            }else if(isSameDay!=false){



                val lastDate = task.lastCompletedDate?.let{Calendar.getInstance().apply { timeInMillis = dateFormat3.parse(it).time }}

                lastDate?.let{

                    val targetDay = lastDate.get(Calendar.DAY_OF_MONTH)
                    val targetMonth = lastDate.get(Calendar.MONTH) +1
                    val targetYear = lastDate.get(Calendar.YEAR)

                    val remainingTimeText = DateUtils.getRemainingTimeText(
                        targetDay = null,
                        targetMonth = null,
                        targetYear = null,
                        targetDate =lastDate.timeInMillis,
                        targetTime = task.lastCompletedDate!!.substringAfter(", "),
                    ).let{
                        if(it.contains("kaldı") && it.replace("kaldı","")==""){
                            "Tamamlandı"
                        }else it.replace("geçmiş","önce yapıldı").replace("kaldı","erken yapıldı")
                    }
                    if(remainingTimeText.trim()!=""){

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if(remainingTimeText.contains("önce")) Icons.Default.Check else Icons.Default.CheckCircleOutline,
                                contentDescription = "Bilgi",
                                tint = Color.Green.
                                copy(alpha = if(remainingTimeText.contains("geçmiş")) 1f else 0.8f),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = remainingTimeText.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Green,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = if(isDarkTheme()) 0.2f else 1f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 1f
                                    )
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }


                    }


                }

            }


            TaskLabel(task.category.let { if(it==TaskCategory.CUSTOM) task.customCategoryDetail?: null else it.turkishName}
                ,task.notification,task.locations, baseColor = task.color?.let{Color(it)}?: MaterialTheme.colorScheme.onSecondary, task.subTasks?.split("||")?.size
            ,task.attachments!=null && task.attachments!="")


            /*



            Row {
                if (task.repetition != null) Icon(Icons.Default.Loop, contentDescription = "Tekrarlayan")
                if (task.notification) Icon(Icons.Default.Notifications, contentDescription = "Bildirim")
                if (!task.locations.isNullOrBlank()) Icon(Icons.Default.Place, contentDescription = "Konum")
                if (!task.attachments.isNullOrBlank()) Icon(Icons.Default.AttachFile, contentDescription = "Eklenti")



            }*/


        }
    }
}

@Composable
fun TaskLabel(
    category: String?=null,
    notification: Boolean,
    location: String?=null,
    baseColor: Color,
    subTasksSize: Int?=0,
    hasAttach: Boolean=false,
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
        if(category!=null){
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = "Kategori Icon", tint = baseColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = category.take(50), style = MaterialTheme.typography.bodyMedium, color = baseColor)
            }
        }

        if(location!=null){
            val locs = location.split("||")
            val text = locs.joinToString(",").take(40).substringBeforeLast(",")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f)) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "", tint = baseColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = text, style = MaterialTheme.typography.bodyMedium, color = baseColor)
            }
        }
        if(subTasksSize!=null && subTasksSize>0){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TableRows, modifier = Modifier.size(16.dp), contentDescription = "", tint = baseColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$subTasksSize", style = MaterialTheme.typography.bodyMedium, color = baseColor)
            }
        }
        if(hasAttach){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Attachment, modifier = Modifier.padding(8.dp), contentDescription = "", tint = baseColor)
            }
        }



        if(notification){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = "", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }


    }






}

@Composable
fun PriorityDotCard(task: Task? = null, isImportant: Boolean = false) {
    val priority = task?.priority?: if(isImportant) TaskPriority.HIGH else TaskPriority.MEDIUM

    if(priority!=TaskPriority.MEDIUM){
        Card(modifier = Modifier
            .size(112.dp,40.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {

            },colors = CardDefaults.cardColors(
            containerColor = when(priority) {
                TaskPriority.LOW -> Color(0x8381C784)
                TaskPriority.MEDIUM -> Color(0x8A64B5F6)
                TaskPriority.HIGH -> Color(0x8AE57373)
                null -> Color.Red
            }
        ), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = priority.toString(), fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(4.dp))
                PriorityDot(priority = priority)
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun PriorityDot(priority: TaskPriority) {
    val color = when (priority) {
        TaskPriority.LOW -> Color(0xFF81C784)     // Yeşilimsi - düşük
        TaskPriority.MEDIUM -> Color(0xFF64B5F6)  // Mavi - normal
        TaskPriority.HIGH -> Color(0xFFE57373)    // Kırmızı - yüksek
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}
