package com.taner.taskly.presentation.ui.screen.task_details

import android.app.Activity
import android.app.ActivityManager.TaskDescription
import android.content.Intent
import android.icu.util.Calendar
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.domain.model.Task
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.getFormattedDate
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.Utils
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.presentation.ui.components.PriorityDotCard
import com.taner.taskly.presentation.ui.screen.add_task.colorList
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taner.taskly.MainActivity
import com.taner.taskly.MainActivity.Companion.REQUEST_CODE_READ_STORAGE
import com.taner.taskly.presentation.ui.components.UniversalFilePreview
import com.taner.taskly.presentation.ui.components.getFileNameFromUri
import com.taner.taskly.presentation.viewmodel.ChangeTaskColorViewModel
import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.isTaskSameDay
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.ui.screen.add_task.FullColorPickerDialog
import java.io.File
import java.util.Date


@Composable
fun TaskDetailScreen(navController: NavController,
                       viewModel: TaskViewModel, taskId: Int?, dayOffset: Int?=null
,changeTaskColorViewModel: ChangeTaskColorViewModel) {

    val shouldRefresh by changeTaskColorViewModel.shouldRefresh

    val scrollState = rememberScrollState()

    /*val task by produceState<Task?>(initialValue = null, taskId) {
        value = taskId?.let { viewModel.getTaskById(it)?.toDomain() }
    }*/

    var task by remember { mutableStateOf<Task?>(null) }
    LaunchedEffect(taskId) {
        if (taskId != null) {
            task = viewModel.getTaskById(taskId)?.toDomain()
        }
    }

    var showDelDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else{


        Column(
            modifier = Modifier.background(if(isLightTheme()) Color.White.copy(0.6f) else Color.Transparent)
                .fillMaxSize()
        ) {


            val baseColor = Color(task!!.color!!)
            val lightColor = baseColor.copy(if(isDarkTheme()) 0.3f else 1f)


            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .background(
                            if(isLightTheme()) (Color.LightGray) else Color/*.Transparent*/(0xA62A2A2A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                    // .border(1.dp, MaterialTheme.colorScheme.onSecondary)
                ) {


                    Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = task?.title.orEmpty(),
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            color = baseColor//MaterialTheme.colorScheme.onSecondary
                            ,overflow = TextOverflow.Ellipsis
                        )


                        task?.let{PriorityDotCard(it)}
                    }

                }




                Spacer(modifier = Modifier.height(8.dp))

                val baseColor = Color(task!!.color!!)
                val lightColor = baseColor.copy(alpha = if(isDarkTheme()) 0.3f else 1f)

                Text(
                    text = task?.description.orEmpty(),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = baseColor,//MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    overflow = TextOverflow.Ellipsis
                )



                Spacer(modifier = Modifier.height(8.dp))

                val calendar = Calendar.getInstance()
                //calendar.add(Calendar.DAY_OF_YEAR, dayOffset ?: 0)

                val dayOfWeek = ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)   // 1-31

                //Toast.makeText(context, "$dayOffset", Toast.LENGTH_SHORT).show()


                var targetDayOfMonth = if(task!!.repetition != TaskRepetition.NONE) {
                    dayOffset?.takeIf { it>0 }?.let{
                        dayOfMonth + it
                    }
                } else null

                var isSameDay = dayOffset?.let{it.takeIf { it>0 }?.let { isTaskSameDay(task!!,dayOfMonth + it.toInt()) }}


                if((!task!!.isCompleted) || (task!!.isCompleted && dayOffset!= null && dayOffset>0)){
                    val remainingTimeText = DateUtils.getRemainingTimeText(
                        targetDay = targetDayOfMonth,
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
                                    if(dayCount>300){
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
                                .padding(horizontal = 24.dp, vertical = 8.dp)
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
                                        color = Color.Black.copy(if(isDarkTheme()) 0.2f else 1f),
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
                    val lastDate = task!!.lastCompletedDate?.let{ java.util.Calendar.getInstance().apply {
                        timeInMillis = dateFormat3.parse(it).time }}



                    lastDate?.let{
                        val remainingTimeText = DateUtils.getRemainingTimeText(
                            targetDay = null,
                            targetMonth = null,
                            targetYear = null,
                            targetDate =lastDate.timeInMillis,
                            targetTime = task!!.lastCompletedDate!!.substringAfter(", "),
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
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
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




                Row(Modifier
                    .fillMaxWidth()
                    .padding(32.dp), horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
                    task!!.time?.let {

                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Image(Icons.Default.AccessTime,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))
                            Text(text = "    $it", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
                        }

                    }


                    @Composable
                    fun addDateText(text: String){

                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                            Image(
                                Icons.Default.CalendarMonth,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier.padding(end = 4.dp))
                            Text(text = "   $text", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    when(task!!.repetition){
                        TaskRepetition.NONE->{
                            val timestamp = task!!.date
                            timestamp?.let{timestamp->

                                task!!.time?.let {
                                    getFormattedDate(timestamp)?.let { it1 -> addDateText(it1) }
                                }


                            }

                            if(timestamp==null){
                                dayOffset?.takeIf { it>0 }?.let{


                                    val calendar = Calendar.getInstance()
                                    calendar.add(Calendar.DAY_OF_YEAR, it )

                                    addDateText(dateFormat2.format(calendar.time))

                                }
                            }
                        }
                        TaskRepetition.WEEKLY->{
                            val days = task!!.days
                            days?.split("||")?.let{


                                addDateText(getWeeklySummaryText(it.map { daysOfWeek.indexOf(it.trim()) }).takeIf { it!=null && !it.contains("Gün:") }
                                    ?: ("Her Hafta " + it.joinToString(", ") + " Günleri"))

                            }

                            if(days==null){
                                dayOffset?.takeIf { it>0 }?.let{


                                    val calendar = Calendar.getInstance()
                                    calendar.add(Calendar.DAY_OF_YEAR, it )

                                    addDateText(dateFormat2.format(calendar.time))

                                }
                            }
                        }
                        TaskRepetition.MONTHLY->{
                            val days = task!!.days

                            days?.split("||")?.map { it.replace("->","").toIntOrNull() }?.filterNotNull()?.let{
                                addDateText(it.map { "${it + 1}" }.joinToString ( ", " ).let {
                                    "Her Ayın " + it + ". Günleri"
                                })
                            }

                            if(days==null){
                                dayOffset?.takeIf { it>0 }?.let{

                                    val calendar = Calendar.getInstance()
                                    calendar.add(Calendar.DAY_OF_YEAR, it )

                                    addDateText(dateFormat2.format(calendar.time))

                                }
                            }
                        }
                        else->{

                            dayOffset?.takeIf { it>0 }?.let{


                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, it )


                                addDateText( dateFormat2.format(calendar.time))

                            }
                        }
                    }


                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                        Image(
                            Icons.Default.Autorenew,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.padding(end = 4.dp))
                        Text(text = " Tekrar: ${task!!.repetition}", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(Modifier
                        .weight(1f)
                        .alpha(if (task!!.notification) 1f else 0.4f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                        Image(
                            Icons.Default.Notifications,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.padding(start = 6.dp))
                        Text(text = " Bildirim: ${if (task!!.notification) "Açık" else "Kapalı"}",
                            color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier)
                    }



                }





                TaskLabel(category =
                    when(task!!.category){
                        TaskCategory.CUSTOM->{
                            task!!.customCategoryDetail?: ""
                        }
                        else -> task!!.category.turkishName
                    }
                    ,
                    status = task!!.status.turkishName,
                    baseColor = baseColor
                    ,)

                Spacer(Modifier.height(8.dp))

                // Yer (Opsiyonel)
                task!!.locations?.split("||")?.joinToString(", ")?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp)) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = "Yer Icon", tint = baseColor?: Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Yer: $it", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
                    }
                }


                task!!.subTasks?.split("||")?.let{

                    Divider(Modifier.padding(vertical = 16.dp,horizontal = 16.dp))

                    Spacer(Modifier.height(32.dp))

                    SubTasksList(it, baseColor){isCheck,inde->

                        val newL = task!!.subTasks!!.split("||").toMutableList()
                        var before = newL[inde]

                        var parts = before.split("<|>")
                        before = "${parts.first()}<|>${if(isCheck) 1 else 0}<|>${parts.getOrNull(2)?.takeIf { it!="" }?:""}"
                        newL[inde] = before

                        val newT = task!!.copy(subTasks = newL.joinToString ("||"))
                        viewModel.updateTask(newT!!.toEntity())

                        task = newT

                    }
                }


                task!!.attachments?.split("||")?.let{

                    Divider(Modifier.padding(vertical = 16.dp,horizontal = 16.dp))

                    Spacer(Modifier.height(32.dp))

                    AttachmentsList(it, baseColor)
                }





            }


            Spacer(Modifier.height(8.dp))

            Divider()

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(Modifier
                    .weight(1f)
                    .clickable {

                        var s =
                            task!!.category!!.ordinal.toString() + "," +
                                    task!!.status!!.ordinal + "," +
                                    task!!.priority!!.ordinal + "," +
                                    task!!.repetition!!.ordinal + ","
                        task!!.customCategoryDetail
                        navController.navigate("add_task/$taskId/$s")
                    }, verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Düzenle",
                        style = MaterialTheme.typography.titleMedium,
                        color = baseColor ,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(2f)

                    )
                    Icon(
                        imageVector =Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.weight(1f)

                    )
                }


                Row (Modifier
                    .weight(1f)
                    .clickable {
                        showDelDialog = true
                    }, verticalAlignment = Alignment.CenterVertically){

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        imageVector =Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier
                    )


                    Text(
                        text = "Sil", modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = baseColor ,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                }


                if(task!!.status != TaskStatus.IN_PROGRESS){


                    Row(Modifier
                        .alpha(1f)
                        .weight(0.8f)
                        .clickable {


                            var isComp = false

                            val newT = task!!.copy(isCompleted = isComp,status = TaskStatus.IN_PROGRESS )

                            viewModel.updateTask(newT!!.toEntity())

                            task = newT

                        }, verticalAlignment = Alignment.CenterVertically){

                        Icon(
                            imageVector =Icons.Default.Timelapse,
                            contentDescription = "Done",
                            tint = MaterialTheme.colorScheme.onSecondary
                            , modifier = Modifier.size(16.dp)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = "   Devam Ediyor  ",
                            style = MaterialTheme.typography.titleMedium,
                            color = baseColor ,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }


                Row(Modifier
                    .alpha(if (task!!.isCompleted) 1f else 0.2f)
                    .weight(1f)
                    .clickable {


                        var isComp = !task!!.isCompleted

                        val newT = task!!.copy(isCompleted = isComp,   lastCompletedDate = (dateFormat3.format(
                            Date()
                        )).takeIf { isComp})

                        if (isComp) {
                            newT!!.status = TaskStatus.COMPLETED
                        } else {
                            newT!!.status = TaskStatus.NOT_STARTED
                        }
                        viewModel.updateTask(newT!!.toEntity())

                        if(isComp){
                            val notificationManager =
                                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(task!!.id)
                            NotificationUtils.cancelScheduledNotification(context,task!!.id)

                            if(task!!.notification) notViewModel?.deleteNotificationsByTaskId(task!!.id)
                        }else{
                            if(task!!.notification) NotificationUtils.addNotificationToDatabase(context,task!!)
                        }

                        task = newT

                    }, verticalAlignment = Alignment.CenterVertically){

                    Icon(
                        imageVector =Icons.Default.Done,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = "${if(!task!!.isCompleted)"Tamamlandı" else "Tamamlanmadı"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = baseColor ,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }


            }

            Spacer(Modifier.height(16.dp))


        }









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

                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(task!!.id)
                        NotificationUtils.cancelScheduledNotification(context,task!!.id)

                       notViewModel?.deleteNotificationsByTaskId(task!!.id)


                        viewModel.delTask(task!!.toEntity())

                        navController.popBackStack()

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

        LaunchedEffect(shouldRefresh) {
            if (shouldRefresh) {


                showColorDialog = true

                changeTaskColorViewModel.refreshDone()
            }
        }
        if(showColorDialog){
            FullColorPickerDialog(
                onDismiss = { showColorDialog = false },
                onColorSelected = {
                    val color = it.hashCode()

                    val newT = task!!.copy(color = color)
                    viewModel.updateTask(newT!!.toEntity())

                    task = newT

                    showColorDialog = false
                }
            )
        }

    }



}

@Composable
fun SubTasksList(list: List<String>, color: Color, onChecked: (Boolean,Int)->Unit) {

    val subList = list
        .groupBy { it.split("<|>").getOrNull(1)?.toIntOrNull() == 1 }
        .toList()
        .sortedBy { it.first }
        .flatMap { it.second }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)) {

        Text(
            text = "${subList.size} Alt Görev",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray ,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()//.background(Color.Gray)
        )



        var showSheet by remember { mutableStateOf(false) }
        var showSheetItemIndex by remember { mutableStateOf(-1) }



        Column(Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            (
                    subList
            ).forEachIndexed { index, sub ->

                val parsed = sub.split("<|>")
                val subTask = parsed.first()
                val isCompleted = parsed.getOrNull(1) == "1"
                val desc = parsed.getOrNull(2)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                        .clickable {
                            showSheetItemIndex = index
                            showSheet = true
                        }
                ) {

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(Modifier.width(16.dp))


                    Checkbox(modifier = Modifier.width(16.dp), onCheckedChange = {


                        onChecked.invoke(it,list.indexOf(sub))

                    },checked = isCompleted)

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = subTask,
                        color = MaterialTheme.colorScheme.onSecondary ,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if(isCompleted) TextDecoration.LineThrough else null
                        )
                    )
                }

                if (index != subList.lastIndex) {
                    Spacer(Modifier.width(16.dp))

                    //Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

        }


        if (showSheet) {
            if(showSheetItemIndex>-1){
                val title = subList[showSheetItemIndex].split("<|>").first()
                val tick = subList[showSheetItemIndex].split("<|>").getOrNull(1) == "1"
                val desc = subList[showSheetItemIndex].split("<|>").getOrNull(2)?.takeIf { it!="null" }

                MyBottomSheetDialog(title,
                    description = desc,
                    onDismiss = "TAMAM" to { showSheet = false },
                    onConfirm = (if(tick) "TAMAMLANMADI" else "TAMAMLANDI") to  {
                        showSheet = false
                    }
                )
            }

        }

    }
}

@Composable
fun AttachmentsList(list: List<String>, color: Color) {

    val context = LocalContext.current

    val attList = list
        .groupBy { it.split("<|>").getOrNull(1)?.toIntOrNull() == 1 }
        .toList()
        .sortedBy { it.first }
        .flatMap { it.second }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)) {

        Text(
            text = "${attList.size} Dosya",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray ,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()//.background(Color.Gray)
        )






        Column(Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            (
                    attList
            ).forEachIndexed { index, file ->


                val uri = file.toUri()


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                        .clickable {


                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, context.contentResolver.getType(uri))
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)

                        }
                ) {

                    Spacer(Modifier.width(16.dp))

                    UniversalFilePreview(uri, 300.dp)

                    Spacer(Modifier.width(8.dp))

                    var t = getFileNameFromUri(context, uri)

                    if(t==null){
                        val f = File(uri.scheme)
                        //t = f.path
                    }

                    Text(
                        text = t?: uri?.lastPathSegment?: "Dosya",
                        color = MaterialTheme.colorScheme.onSecondary ,
                        style = MaterialTheme.typography.bodyLarge
                    )



                }

                if (index != attList.lastIndex) {
                    Spacer(Modifier.width(16.dp))

                    //Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

        }



    }
}


@Composable
fun TaskLabel(
    category: String,
    status: String,
    baseColor:Color = Color.Blue
) {

    var statusType =  when (status) {
        "TAMAMLANDI" -> 2
        "Başlamadı" -> 0
        "Devam Ediyor" -> 1
        else -> Icons.Default.Error
    }
    val statusIcon = when (statusType) {
        2 -> Icons.Default.CheckCircle
        0,1 -> Icons.Default.RadioButtonUnchecked
        else -> Icons.Default.Error
    }
    val statusColor = when (statusType) {
        2 -> Color.Green
        0 -> Color.Gray
        1 -> if(isLightTheme()) Color.Cyan else Color.Yellow
        else -> Color.Red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, statusColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kategori
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = "Kategori Icon", tint = baseColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Kategori: $category", style = MaterialTheme.typography.bodyMedium)
        }

        // Durum
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = statusIcon, contentDescription = "Durum Icon", tint = statusColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Durum: $status", style = MaterialTheme.typography.bodyMedium)
        }
    }






}



@Composable
fun MyBottomSheetDialog(
    title: String,
    description: String?=null,
    onDismiss: (Pair<String, ()->Unit>),
    onConfirm: (Pair<String, ()->Unit>)?=null
) {
    Dialog(onDismissRequest = onDismiss.second) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp, modifier = Modifier
                .width(500.dp)
                .sizeIn(maxHeight = 700.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(maxHeight = 630.dp)
                        //.fillMaxHeight()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(1){
                        Text(text = title, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))



                        description?.let{

                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = it, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodyMedium)

                        }
                        Spacer(modifier = Modifier.height(16.dp))

                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,

                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom
                ) {
                    Button(onClick = onDismiss.second) {
                        Text(onDismiss.first, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    onConfirm?.let{
                        Button(onClick = it.second) {
                            Text(it.first, color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

            }
        }
    }
}



