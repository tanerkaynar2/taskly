package com.taner.taskly.presentation.ui.screen.habits

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.icu.text.DateFormat
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColor
import androidx.navigation.NavController
import com.taner.taskly.MainActivity.Companion.isDarkTaskCard
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.core.utils.AchievementUtils
import com.taner.taskly.core.utils.DateUtils.Companion.countMatchingDays
import com.taner.taskly.core.utils.DateUtils.Companion.countMatchingWeekdays
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat4
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.getDayOfYearListForMonth
import com.taner.taskly.core.utils.DateUtils.Companion.getDayOfYearListForWeekdays
import com.taner.taskly.core.utils.DateUtils.Companion.getDaysOfMonth
import com.taner.taskly.core.utils.DateUtils.Companion.getDaysOfMonthForWeekdays
import com.taner.taskly.core.utils.DateUtils.Companion.getDaysOfMonthForWeekdays2
import com.taner.taskly.core.utils.DateUtils.Companion.getFirstWeekdayOfMonth
import com.taner.taskly.core.utils.DateUtils.Companion.getHourAndMinuteFromTimestamp
import com.taner.taskly.core.utils.DateUtils.Companion.getMonthRange
import com.taner.taskly.core.utils.DateUtils.Companion.getRemainingTimeText
import com.taner.taskly.core.utils.DateUtils.Companion.getRemainingTimeText2
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.DateUtils.Companion.monthNames
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.ui.components.CompletionGraph
import com.taner.taskly.presentation.ui.components.CompletionGraphOptions
import com.taner.taskly.presentation.ui.components.FrequencyChartCircleStyle
import com.taner.taskly.presentation.ui.components.HabitCalendarView
import com.taner.taskly.presentation.ui.components.HourlyTrendChart
import com.taner.taskly.presentation.ui.components.PriorityDotCard
import com.taner.taskly.presentation.ui.components.TaskLabel
import com.taner.taskly.presentation.ui.components.habitCalendarValues
import com.taner.taskly.presentation.ui.screen.add_task.FullColorPickerDialog
import com.taner.taskly.presentation.ui.screen.add_task.SimpleDialog
import com.taner.taskly.presentation.viewmodel.ChangeTaskColorViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfMonth
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfYear
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.getExceptedDays
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.getPreviousRepeatDay
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.monthIndex
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.year
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashMap
import kotlin.math.abs

@Composable
fun HabitDetailsScreen(
    habitId: Int,
    navController: NavController,
    viewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    changeTaskColorViewModel: ChangeTaskColorViewModel
) {

    val shouldRefresh by changeTaskColorViewModel.shouldRefreshForHabit
    val shouldRefreshEditDialog by changeTaskColorViewModel.shouldRefreshForHabitEditDialog
    val shouldRefreshDelDialog by changeTaskColorViewModel.shouldRefreshForHabitDelDialog

    val context = LocalContext.current

    var showLongClickDialog by remember { mutableStateOf(false) }
    var showDelDialog by remember { mutableStateOf(false) }
    var successRate by remember { mutableStateOf(0) }

    var showColorDialog by remember { mutableStateOf(false) }

    var showAddNoteDialog by remember { mutableStateOf(false) }

    var cColor by remember { mutableStateOf(Color.Transparent) }
    var showRefreshBtn by remember { mutableStateOf(false) }

    Column(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(cColor)) {

        val habit by produceState<Habit?>(initialValue = null, habitId) {
            value = viewModel.getHabitById(habitId)?.toDomain()
        }


        val clickType = remember { mutableStateOf(999) }//0 yapıldı   1 yapılmadı   2 boş      3 not  4// baş. gün. değiştir
        var changeCreatedClickMode by remember { mutableStateOf<Int>(0) }
        var addNoteDialog by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var checkedCount by remember { mutableStateOf<Int>(0) }

        @Composable
        fun HabitDetailCard(h: Habit) {


            var habit by remember { mutableStateOf(h) }



            if(showColorDialog){
                FullColorPickerDialog(
                    onDismiss = { showColorDialog = false },
                    onColorSelected = {
                        val color = it.hashCode()

                        val newH = habit!!.copy(color = color)
                        viewModel.updateHabit(newH!!.toEntity())

                        habit = newH

                        showColorDialog = false
                    }
                )
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
                            viewModel.delHabit(habit.id,context,All = null)

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

            if(showLongClickDialog){
                AlertDialog(
                    onDismissRequest = { showLongClickDialog = false },
                    title = { Text(habit.name,color = MaterialTheme.colorScheme.onSecondary) },
                    text = { Text("",color = MaterialTheme.colorScheme.onSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            showAddNoteDialog = true
                            showLongClickDialog = false
                        }) {
                           Text(if(habit.note?.takeIf { it!="" }!=null) "Notu düzenle" else "Not Ekle",color = Color.Cyan)
                        }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                viewModel.activeToggleHabit(habit.id,All = null)
                                habit.isActive =!habit.isActive
                                showLongClickDialog = false
                            }) {
                                Text( if(habit.isActive)"Pasif   " else "Aktif   ",color = MaterialTheme.colorScheme.onSecondary)
                            }
                            TextButton(onClick = {
                                navController.navigate("add_habit/${habit.id}")
                                showLongClickDialog = false
                            }) {
                                Text("Düzenle",color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }

                    }
                )
            }

            if(showAddNoteDialog){
                SimpleDialog(currentStringData = habit.note?.takeIf { it!="" },
                    action = "Kaldır" to {
                        showAddNoteDialog = false
                        habit.note = null
                        viewModel.updateHabit(habit.copy(note = null).toEntity())
                    }, title = "Notlarım", onDismiss = {
                        showAddNoteDialog = false
                    }, onConfirm = {
                        habit.note = it
                        showAddNoteDialog = false
                        viewModel.updateHabit(habit.copy(note = it).toEntity())
                    })
            }



            val containerColor = (if(isDarkTaskCard){

                if(isDarkTheme()) listOf(
                    Color(0xFF2C2C2C),
                    Color(0xFF1A1A1A)
                ) else listOf(Color(0xFFB0B0B0), Color(0xFF9E9E9E))

            } else {
                (listOf(
                    Color(habit.color!! ).copy(
                        if(isDarkTheme() || (isLightTheme() && habit.color == Color.White.hashCode())) 0.1f else 1f),

                    Color(habit.color!! ).copy(if(isDarkTheme()  || (isLightTheme() && habit.color == Color.White.hashCode())) 0.3f else 1f)/*Color(0xFF1F2633), Color(0xFF332B1F)*//*Color.DarkGray,Color.Gray*/)
                        )
            }.get((0..1).random())).let{
                if(!habit.isActive) it.copy(0.009f) else it
            }


            val animatedAlpha = animateFloatAsState(
                targetValue = 0.01f,
                animationSpec = tween(durationMillis = 4000)
            ).value

            val animatedColor = containerColor.copy(alpha = animatedAlpha)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ,colors = CardDefaults.cardColors(
                    containerColor = animatedColor//task.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
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

                        Column(Modifier
                            .weight(2f)
                            .padding(vertical = 4.dp)) {



                            Row(Modifier.fillMaxWidth()){

                                Text(text = habit.name, color = habit.color?.let{Color(it)}?: MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        textDecoration = if(!habit.isActive) TextDecoration.LineThrough else null
                                    ), modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.Center, fontSize = 25.sp)




                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                                    //Spacer(modifier = Modifier.width(36.dp))
                                    PriorityDotCard(null, isImportant = habit.isImportant)
                                }
                            }



                            habit.explain.takeIf { it!="" }?.let {
                                Text(text = it, color = Color(0xFFDDDDDD), style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Start,)
                            }

                            Spacer(Modifier.height(8.dp))

                            Divider()

                            Spacer(Modifier.height(8.dp))

                            Row(Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                habit.reminderTimeStamp?.let {

                                    getHourAndMinuteFromTimestamp(it).let {
                                        it.toList().joinToString(":").let{
                                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    Icons.Default.Notifications,"", colorFilter = ColorFilter.tint(
                                                        habit.color?.let{Color(it)}?: MaterialTheme.colorScheme.onSecondary))
                                                Text(text = "    $it", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
                                            }
                                        }
                                    }

                                }


                                @Composable
                                fun addDateText(text: String){
                                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                        Image(
                                            Icons.Default.CalendarMonth,"", colorFilter = ColorFilter.tint(
                                                habit.color?.let{Color(it)}?: MaterialTheme.colorScheme.onSecondary),
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


                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                TaskLabel(habit.category,habit.color?.let{ Color(it) }?: Color.White)
                            }


                            habit.note.takeIf { it!="" }?.let {
                                Spacer(Modifier.height(8.dp))

                                Divider()

                                Spacer(Modifier.height(8.dp))


                                Text(text = "Notlar           \n", color =  habit.color?.let{Color(it)}?: if(!isLightTheme()) Color.White else Color.Black,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center,)

                                Text(text = it, color = Color(0xFFDDDDDD), style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Start,)
                            }

                            Spacer(Modifier.height(8.dp))

                            Divider()

                            Spacer(Modifier.height(8.dp))

                            Row(Modifier
                                .fillMaxWidth()
                                .padding(16.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {



                                Row(Modifier
                                    .weight(1f)
                                    .padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                    Image(
                                        Icons.Default.MilitaryTech,"", colorFilter = ColorFilter.tint(
                                            habit.color?.let{Color(it)}?:  MaterialTheme.colorScheme.onSecondary),
                                        modifier = Modifier.padding(end = 4.dp))
                                    Text(text = "   En Uzun seri ${habit.longestStreak} Gün",
                                        color = if(habit.currentStreak==habit.longestStreak && habit.longestStreak!=0) Color.Green else MaterialTheme.colorScheme.onSecondary,
                                        textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                                }

                                habit.longestStreakDayOfYear?.let{day->
                                    habit.longestStreakYear?.let{year->
                                        Row(Modifier
                                            .weight(1f)
                                            .padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                            Image(
                                                Icons.Default.MilitaryTech,"", colorFilter = ColorFilter.tint(
                                                    habit.color?.let{Color(it)}?:  MaterialTheme.colorScheme.onSecondary),
                                                modifier = Modifier.padding(end = 4.dp))
                                            Text(text = " ${
                                                Calendar.getInstance().apply {
                                                    set(Calendar.DAY_OF_YEAR,day)
                                                    set(Calendar.YEAR,year)
                                                }.let{ dateFormat.format(it.timeInMillis)}} ", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }


                                Row(Modifier
                                    .weight(1f)
                                    .padding(4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

                                    Image(
                                        Icons.Default.Whatshot,"", colorFilter = ColorFilter.tint(
                                            habit.color?.let{Color(it)}?:  MaterialTheme.colorScheme.onSecondary),
                                        modifier = Modifier.padding(end = 4.dp))
                                    Text(text = "   Mevcut seri ${habit.currentStreak} Gün",
                                        color = if(habit.currentStreak==habit.longestStreak && habit.longestStreak!=0) Color.Green else
                                            MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall)
                                }


                            }


                            Spacer(Modifier.height(8.dp))

                            Divider()

                            Spacer(Modifier.height(8.dp))


                        }

                    }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 48.dp),
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
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterEnd)
                            .padding(top = 16.dp, end = 16.dp),
                        fontSize = 10.sp
                    )
                }
                LaunchedEffect(habit) {
                    CoroutineScope(Dispatchers.IO).launch {
                        successRate = habitCheckViewModel.calculateSuccessRate(habit.id)
                    }
                }

            }



            Spacer(Modifier.height(40.dp))

            val expectedDays = getExceptedDays(habit)
            val currentStreak = habit.currentStreak
            val longestStreak = habit.longestStreak
            var successRate by remember { mutableStateOf<Int>(0) }
            var successColor by remember { mutableStateOf(Color(0xFFF44336)) }
            var averageCompletionTime by remember { mutableStateOf<String?>(null) }
            var lastCheck by remember { mutableStateOf<HabitCheck?>(null) }




            LaunchedEffect(habit) {
                successRate = habitCheckViewModel.calculateSuccessRate(habitId)
                checkedCount = habitCheckViewModel.getCheckedCount(habitId)
                averageCompletionTime = habitCheckViewModel.getAverageCompletionTime(habitId)?.let{
                    getHourAndMinuteFromTimestamp(it).let{"${it.first}:${it.second}"}
                }
                successColor = when {
                    successRate >= 90 -> Color(0xFF4CAF50) // Gold
                    successRate >= 50 -> Color(0xFFFFD700) // Green
                    else -> Color(0xFFF44336)             // Red
                }
                lastCheck = habitCheckViewModel.getLastHabitCheck(habitId)?.toDomain()
            }

            if(!showRefreshBtn) Divider()
            Spacer(Modifier.height(16.dp))


            val myCalendar = Calendar.getInstance().apply {
                timeInMillis = habit.createdAt
            }


            if(showRefreshBtn){

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(4.dp)) {

                    Divider(Modifier.weight(2f))

                    IconButton(modifier = Modifier
                        .weight(1f), onClick = {
                        showRefreshBtn = false
                        CoroutineScope(Dispatchers.IO).launch {
                            val refreshedHabit = viewModel.getHabitById(habit.id)
                            refreshedHabit?.toDomain()?.let{habit = it}
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Yenile")
                    }


                    Divider(Modifier.weight(2f))


                }


            }

            label1("\uD83D\uDDD3\uFE0F Takip Süresi: $expectedDays Gün")
            label1("\uD83D\uDCC8 Devam Serisi: ", text2 = "$currentStreak", text3 = " Gün",
                color2 = Color.Green.takeIf { currentStreak == longestStreak && currentStreak!=0 })
            label1("\uD83D\uDD25 En uzun seri: ", text2 = "$longestStreak", text3 = " Gün${
                if(habit.longestStreakYear!=null && habit.longestStreakDayOfYear!=null && longestStreak>1){
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR,habit.longestStreakYear!!)
                        set(Calendar.DAY_OF_YEAR,habit.longestStreakDayOfYear!!)
                        set(Calendar.HOUR_OF_DAY,0)
                        set(Calendar.MINUTE,0)
                        set(Calendar.MILLISECOND,0)
                    }.let { 
                        "     |    " + dateFormat.format(it.timeInMillis) +  (getRemainingTimeText2(
                            targetDay = null,
                            targetMonth = null,
                            targetYear = null,
                            targetDate = it.timeInMillis
                            , targetTime = null
                        ).takeIf { it!="" }?.takeIf { it.trim()!="kaldı" }?.let {"   |   $it"}?.replace("-","")?: "")
                    }
                }else ""
            }",
                color2 = Color.Green.takeIf { currentStreak == longestStreak && longestStreak!=0})
            label1("\uD83C\uDFAF Başarı Oranı: ", text2 = "$successRate", text3 = "%",
                color2 =  successColor)
            label1("\uD83D\uDCCA Tamamlanan Gün: ", text2 = "$checkedCount", text3 = "/$expectedDays",
                color2 =  successColor)
            averageCompletionTime?.let{label1("⏰ Ortalama Saat: $it")}


            label1("\uD83D\uDCA1 İlk Gün: ${dateFormat3.format(Date(myCalendar.timeInMillis))}"){

            }





            lastCheck?.let{
                label1("\uD83C\uDFC1 Son Gün: ", text2 = dateFormat3.format(Date(myCalendar.apply {
                    set(Calendar.YEAR,it.year)
                    set(Calendar.DAY_OF_YEAR,it.dayOfYear)
                    getHourAndMinuteFromTimestamp(it.time).let{
                        set(Calendar.HOUR_OF_DAY,it.first)
                        set(Calendar.MINUTE,it.second)
                    }
                }.timeInMillis)),
                    color2 = Color.Red.takeIf { c->!it.isChecked }, text3 = getRemainingTimeText2(
                        targetDay = null,
                        targetMonth = null,
                        targetYear = null,
                        targetDate = myCalendar.timeInMillis
                    , targetTime = myCalendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + myCalendar.get(Calendar.MINUTE).toString()
                    ).takeIf { it!="" }?.takeIf { it.trim()!="kaldı" }?.let { "     |    $it" }?.replace("-",""))
            }


        }





        habit?.let {habit->
            HabitDetailCard(habit)

            Spacer(Modifier.height(40.dp))




            Divider()


            Text("\n\n  Takvim  \n\n", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())


            val startDayOfYearsAndYear = remember { mutableStateOf(Calendar.getInstance().apply {
                timeInMillis = habit.createdAt
            }.let { it.get(Calendar.DAY_OF_YEAR) to it.get(Calendar.YEAR) }) }

            Box(Modifier.height(40.dp)){
                Row(Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        "     ${dateFormat.format(Date())}", modifier = Modifier.weight(2f),
                        color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp, textAlign = TextAlign.Start)


                    if(clickType.value == 999){
                        Row (Modifier
                            .padding(4.dp)
                            .weight(2.5f)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    clickType.value = 0
                                }
                            }
                            , horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {


                            Image(Icons.Default.Edit,"",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary), modifier = Modifier
                                    .size(24.dp)
                                    .padding(horizontal = 6.dp))

                            Text("  Düzenle   ",
                                color = MaterialTheme.colorScheme.onSecondary, fontSize = 9.sp)

                        }

                    }else{
                        Row (Modifier
                            .padding(4.dp)
                            .weight(1f)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    clickType.value = 999
                                }
                            }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                            Text("  TAMAM   ",
                                color = MaterialTheme.colorScheme.onSecondary, fontSize = 9.sp)

                            Image(Icons.Default.NavigateNext,"",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))
                        }

                        Image(Icons.Default.Info,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                            modifier =  Modifier
                                .size(16.dp)
                                .clickable {
                                    Toast.makeText(
                                        context,
                                        "Günleri işaretlemek ve not eklemek için takvimde bir günü seç. ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })

                        Row (Modifier
                            .padding(4.dp)
                            .weight(1.5f)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    clickType.value = 0
                                }
                            }

                            .border(
                                if (clickType.value == 0 || clickType.value == 1) {
                                    1.dp
                                } else 0.dp,
                                if (clickType.value == 0 || clickType.value == 1) MaterialTheme.colorScheme.onSecondary
                                else Color.Transparent
                            ), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                            Box(Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (clickType.value == 0)
                                        Color.Green.copy(0.7f) else if (clickType.value == 1)
                                        Color.Red.copy(0.4f) else Color.Green.copy(0.7f)
                                )){
                            }

                            Text("  Yapıldı/Yapılmadı   ",
                                color = MaterialTheme.colorScheme.onSecondary, fontSize = 9.sp)

                        }



                        Row(Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    clickType.value = 2
                                }
                            }
                            .border(
                                if (clickType.value == 2) {
                                    1.dp
                                } else 0.dp,
                                if (clickType.value == 2) MaterialTheme.colorScheme.onSecondary
                                else Color.Transparent
                            ), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                            Box(Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(0.1f))){
                            }

                            Text("     Boş ",
                                color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp)

                        }

                    }

                    Row(Modifier
                        .padding(8.dp)
                        .weight(1f)
                        .clickable {
                            //clickType.value = 3
                            showAddNoteDialog = true

                        }
                        .border(
                            if (clickType.value == 3) {
                                1.dp
                            } else 0.dp,
                            if (clickType.value == 3) MaterialTheme.colorScheme.onSecondary
                            else Color.Transparent
                        ), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {


                        Text("  Not Ekle   ",
                            color = MaterialTheme.colorScheme.onSecondary, fontSize = 10.sp)


                        Image(Icons.Default.EditNote,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))

                    }

                }
                }



            if(clickType.value != 999){
                Row(Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically){
                    Row(Modifier
                        .padding(8.dp)
                        .clickable {
                            CoroutineScope(Dispatchers.Main).launch {
                                clickType.value = if (clickType.value == 3) {
                                    changeCreatedClickMode = 0
                                    0
                                } else {
                                    changeCreatedClickMode = 5
                                    Toast.makeText(
                                        context,
                                        "Başlangıç gününü değiştirmek için bir gün seç",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    4
                                }
                            }
                        }
                        .border(
                            if (clickType.value == 4) {
                                1.dp
                            } else 0.dp,
                            if (clickType.value == 4) MaterialTheme.colorScheme.onSecondary
                            else Color.Transparent
                        ), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ModeEdit,"", tint =
                            if(clickType.value == 4) Color.Magenta else MaterialTheme.colorScheme.onSecondary, modifier =
                            Modifier
                                .size(24.dp)
                                .padding(horizontal = 6.dp)
                                .clickable {

                                })
                        Text("  Başlangıç gününü değiştir ",
                            color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp)

                    }


                    Row(Modifier
                        .padding(8.dp)
                        .clickable {
                            CoroutineScope(Dispatchers.Main).launch {
                                clickType.value = 6
                            }
                        }
                        .border(
                            if (clickType.value == 6) {
                                1.dp
                            } else 0.dp,
                            if (clickType.value == 6) MaterialTheme.colorScheme.onSecondary
                            else Color.Transparent
                        ), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote,"", tint =
                            if(clickType.value == 6) Color.Magenta else MaterialTheme.colorScheme.onSecondary, modifier =
                            Modifier
                                .size(24.dp)
                                .padding(horizontal = 6.dp)
                                .clickable {

                                })
                        Text("  Güne Not Ekle ",
                            color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp)

                    }





                }
            }


            Spacer(Modifier.height(4.dp))

            val hm = remember { mutableStateOf(hashMapOf<Pair<Int, Int>, habitCalendarValues>()) }


            if(addNoteDialog!=null){

                var beforeCheck = hm.value[addNoteDialog]

                SimpleDialog(currentStringData = beforeCheck?.highlightedExplain?.takeIf { it!="" },
                    action = "Kaldır" to {
                        beforeCheck?.highlightedExplain = null
                        beforeCheck?.isHighlightedDay = false
                        habitCheckViewModel.update(habitId,addNoteDialog!!.first,addNoteDialog!!.second,null)
                        hm.value[addNoteDialog!!] = beforeCheck!!
                        addNoteDialog=null
                    }, title = "Notlarım", onDismiss = {
                        addNoteDialog=null
                    }, onConfirm = {
                        if(beforeCheck==null){
                            habitCheckViewModel.toggleCheck(habitId,addNoteDialog!!.first,addNoteDialog!!.second,false,
                                note = it)


                            addNoteDialog=null

                        }else{
                            beforeCheck?.highlightedExplain = it
                            beforeCheck?.isHighlightedDay = true
                            beforeCheck?.highlightedDayColor = Color.Cyan.copy(if(!isLightTheme())0.8f else 1f)
                            beforeCheck?.highlightedDayIcon = Icons.Default.Notes
                            habitCheckViewModel.update(habitId,addNoteDialog!!.first,addNoteDialog!!.second,it)
                            hm.value[addNoteDialog!!] = beforeCheck
                            addNoteDialog=null
                        }
                    })
            }



            val monthOffsetValue = remember { mutableStateOf(0) }

            var todayMonthIndex =  Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_MONTH,1)
            }.get(Calendar.MONTH)
            var monthCount = 3
            var monthIndexesAndYear by remember { mutableStateOf(
                List(monthCount){

                    var todayMonth = todayMonthIndex-monthOffsetValue.value-it
                    var yearOffset = 0
                    fun next(){
                        if(todayMonth<0){
                            yearOffset++
                            todayMonth+=12
                            next()
                        }
                    }
                    next()

                    (todayMonth to (year- yearOffset))
                }.reversed()
            ) }

            val maxMonthIndexAndYear = ((todayMonthIndex+1).let{if(it<=11) it else todayMonthIndex} ) to year

            val highlightedExplain= "En uzun seri"

            var changeCreatedClickModeSelectedDayOfYearAndYear by remember { mutableStateOf<Pair<Int, Int>?>(null) }
            var changeCreatedClickModeDayOfYearAndYear by remember { mutableStateOf<Pair<Int, Int>?>(null) }


            @Composable
            fun setHabitCalendar(){

                var currentDayOfYear by remember { mutableStateOf(dayOfYear) }
                var currentYear by remember { mutableStateOf(year) }

                if(changeCreatedClickMode==4){


                    changeCreatedClickModeDayOfYearAndYear?.let{

                        val cal = Calendar.getInstance().apply {
                            timeInMillis = habit.createdAt

                            set(Calendar.DAY_OF_YEAR,it.first)
                            set(Calendar.YEAR,it.second)
                        }

                        AlertDialog(
                            onDismissRequest = {
                                changeCreatedClickMode = 0
                            },
                            title = {
                                Text(
                                    text = dateFormat.format(Date(cal.timeInMillis)),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            },
                            text = {
                                Text(
                                    text = "${dateFormat4.format(Date(cal.timeInMillis))}\n\n${
                                        getRemainingTimeText2(
                                            targetDay = null,
                                            targetMonth = null,
                                            targetYear = null,
                                            targetDate = cal.timeInMillis,
                                            targetTime = timeFormat.format(Date(cal.timeInMillis))
                                        )
                                    }",
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    changeCreatedClickMode = 0
                                }) {
                                    Text(
                                        text = "Tamam",
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            },
                            dismissButton = {}
                        )
                    }

                }

                if(changeCreatedClickMode==1){
                    AlertDialog(
                        onDismissRequest = {
                            changeCreatedClickMode = 0
                        },
                        title = { Text("Başlangıç gününü değiştir?", color=MaterialTheme.colorScheme.onSecondary) },
                        confirmButton = {
                            TextButton(onClick = {
                                changeCreatedClickMode = 2
                            }) {
                                Text("Farklı Bir Gün Seç", color =MaterialTheme.colorScheme.onSecondary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                changeCreatedClickMode = 3
                            }) {
                                Text("Seç: ${Calendar.getInstance().apply {
                                    changeCreatedClickModeSelectedDayOfYearAndYear?.let{
                                        set(Calendar.DAY_OF_YEAR,it.first)
                                        set(Calendar.YEAR,it.second)
                                    }
                                }.let{ dateFormat.format(Date(it.timeInMillis))}
                                }", color=MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    )
                }

                if(changeCreatedClickMode == 3){
                    AlertDialog(
                        onDismissRequest = {
                            changeCreatedClickMode = 0


                        },
                        title = { Text("Emin misiniz?", color=MaterialTheme.colorScheme.onSecondary) },
                        text = {
                            Text("Başlangıç Gününü değiştirmek istediğinize Emin misiniz?\nBu işlem geri alınamaz. Mevcut serileriniz sıfırlanır ve Seçilen günden önceki tamamlamanan günler de silinir.", color=MaterialTheme.colorScheme.onSecondary)
                        },
                        confirmButton = {
                            TextButton(onClick = {

                                changeCreatedClickModeSelectedDayOfYearAndYear?.let{v->
                                    Calendar.getInstance().apply {
                                        timeInMillis = habit.createdAt
                                        set(Calendar.DAY_OF_YEAR,v.first)
                                        set(Calendar.YEAR,v.second)
                                    }.let{
                                        viewModel.updateHabit(habit.let{h->

                                            if(h.frequency == HabitFrequency.MONTHLY){
                                                val beforeDays = h.days.toMutableList()

                                                val dayOfMonth = it.get(Calendar.DAY_OF_MONTH)
                                                if(!beforeDays.contains(dayOfMonth)) beforeDays.add(dayOfMonth)

                                                h.copy(days = beforeDays)
                                            }else h

                                        }.toEntity().copy(createdAt =
                                            it.timeInMillis))

                                        habitCheckViewModel.deleteChecksBefore(habitId,
                                            v.first,v.second)//it.get(Calendar.DAY_OF_YEAR), it.get(Calendar.YEAR))

                                    }

                                    navController.popBackStack()
                                    navController.navigate("habit_details/$habitId")

                                }

                                changeCreatedClickMode = 0
                            }) {
                                Text("Evet", color=MaterialTheme.colorScheme.onSecondary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                changeCreatedClickMode = 0
                            }) {
                                Text("Vazgeç", color=MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    )
                }

                HabitCalendarView(
                    startDayOfYearsAndYear = startDayOfYearsAndYear.value,
                    modifier = Modifier,
                    onBeforeStartDayClick = {
                        showRefreshBtn = true
                        if(clickType.value==4){if(changeCreatedClickMode==0 || changeCreatedClickMode==4 || changeCreatedClickMode==5){
                            changeCreatedClickModeSelectedDayOfYearAndYear=it
                            changeCreatedClickMode = 1
                        }else if(changeCreatedClickMode == 2){
                            changeCreatedClickModeSelectedDayOfYearAndYear=it
                            changeCreatedClickMode = 3
                        }}
                    },
                    onAfterStartDayClick = {
                        if(clickType.value != 999){
                            //showRefreshBtn = true

                            changeCreatedClickMode = 4
                            clickType.value = 0
                        }
                        changeCreatedClickModeDayOfYearAndYear = it
                    },
                    todayDayOfYear = currentDayOfYear,
                    todayYear = currentYear,
                    monthIndexesAndYear = monthIndexesAndYear,
                    nextBtn = {


                        fun calc(it: Int): Pair<Int, Int> {
                            var todayMonth = todayMonthIndex-monthOffsetValue.value-it
                            var yearOffset = 0
                            fun next(){
                                if(todayMonth<0){
                                    yearOffset++
                                    todayMonth+=12
                                    next()
                                }
                            }
                            next()

                            return todayMonth to yearOffset
                        }

                        if(monthOffsetValue.value>-2){

                            monthOffsetValue.value = monthOffsetValue.value - 1
                            monthIndexesAndYear =
                                List(monthCount){

                                    val (todayMonth, yearOffset) = calc(it)

                                    (todayMonth to (year- yearOffset))
                                }.reversed()
                        }else{

                        }

                    },
                    previousBtn = {
                        monthOffsetValue.value = monthOffsetValue.value +1
                        monthIndexesAndYear =
                            List(monthCount){

                                var todayMonth = todayMonthIndex-monthOffsetValue.value-it
                                var yearOffset = 0
                                fun next(){
                                    if(todayMonth<0){
                                        yearOffset++
                                        todayMonth+=12
                                        next()
                                    }
                                }
                                next()

                                (todayMonth to (year- yearOffset))
                            }.reversed()
                    },
                    goToHabitStartDayBtn = {
                        val habitMonthIndexAndYear = Calendar.getInstance().apply {
                            timeInMillis = habit.createdAt
                        }.let{
                            (it.get(Calendar.MONTH) to it.get(Calendar.YEAR))
                        }
                        val yearDiff = year - habitMonthIndexAndYear.second



                        monthOffsetValue.value =  abs((yearDiff) * 12 + (monthIndex - habitMonthIndexAndYear.first)) - monthCount +1

                        monthIndexesAndYear =
                            List(monthCount){

                                var todayMonth = todayMonthIndex-monthOffsetValue.value-it
                                var yearOffset = 0
                                fun next(){
                                    if(todayMonth<0){
                                        yearOffset++
                                        todayMonth+=12
                                        next()
                                    }
                                }
                                next()

                                (todayMonth to (year- yearOffset))
                            }.reversed()
                    },
                    goToToDayBtn = {

                        monthOffsetValue.value =  0

                        monthIndexesAndYear =
                            List(monthCount){

                                var todayMonth = todayMonthIndex-monthOffsetValue.value-it
                                var yearOffset = 0
                                fun next(){
                                    if(todayMonth<0){
                                        yearOffset++
                                        todayMonth+=12
                                        next()
                                    }
                                }
                                next()

                                (todayMonth to (year- yearOffset))
                            }.reversed()
                    },
                    habitDaysOfWeek = if(habit.frequency == HabitFrequency.WEEKLY){
                        habit.days.map { it-1 }
                    }else null,
                    habitDaysOfMonth = if(habit.frequency == HabitFrequency.MONTHLY){
                        habit.days.map { it }
                    }else null,
                    //missedDays = emptySet(),
                    //checkedDays = emptySet(),
                    //highlightedDays = emptyList(),
                    showMonthLabels = true,
                    //checkDaysColor=Color.Green.copy(0.7f),
                    //missedDaysColor=Color.Red.copy(0.4f),
                    //highlightedDaysColor= mapOf(),
                    //defaultColor = Color.Gray.copy(0.1f),
                    loadCheckValues = {h->

                        val it = h.toMutableList()

                        val newC = Calendar.getInstance().apply {
                            h.lastOrNull()?.first?.let { it1 -> set(Calendar.DAY_OF_YEAR, it1) }
                            h.lastOrNull()?.second?.let { it1 -> set(Calendar.YEAR, it1) }
                            add(Calendar.MONTH,1)
                            set(Calendar.DAY_OF_MONTH,1)
                        }
                        val days = getDayOfYearListForMonth(newC.get(Calendar.MONTH),newC.get(Calendar.YEAR))
                        days.forEach {v->
                            it.add(v to newC.get(Calendar.YEAR))
                        }




                        val notInDayOfYears = it.map { it }
                            .filterNot { dayOfYear -> hm.value.keys.any { it == dayOfYear } }


                        if(notInDayOfYears.isNotEmpty()){
                            val data = habitCheckViewModel.getChecksForDays(habitId, dayOfYears = notInDayOfYears.map { it.first },
                                years = notInDayOfYears.map{it.second})


                            data.let {


                                val newHm = hm.value

                                it.filterNotNull().forEach {
                                    val dayOfYear = it.dayOfYear
                                    val isHighlightedDay = (dayOfYear == habit.longestStreakDayOfYear && it.year == habit.longestStreakYear)||
                                            it.note.trim()!=""
                                    newHm[dayOfYear to it.year] = habitCalendarValues(
                                        dayOfYear = dayOfYear,
                                        year = it.year,
                                        isMissedDay =  !it.isChecked,
                                        isCheckedDay =  it.isChecked,
                                        isHighlightedDay = isHighlightedDay,
                                        highlightedExplain =it.note.takeIf { it.trim()!="" }?: (highlightedExplain.takeIf { isHighlightedDay })
                                        , highlightedDayColor = if(it.note.trim()!="") Color.Cyan.copy(if(!isLightTheme())0.8f else 1f)
                                        else Color.Yellow.copy(0.5f),
                                        highlightedDayIcon = if(it.note.trim()!="") Icons.Default.Notes else Icons.Default.MilitaryTech
                                    )


                                }

                                hm.value = newHm
                            }
                        }


                        hm.value
                    }
                ) {

                    val isChecked = !(it.second.second)
                    val dayOfYear = it.first.first
                    val dayOfMonth = it.second.first
                    val year = it.first.second


                    if(clickType.value != 999){

                        if(changeCreatedClickMode==5 || changeCreatedClickMode==2){
                            showRefreshBtn = true
                            changeCreatedClickModeSelectedDayOfYearAndYear = dayOfYear to year
                            changeCreatedClickMode = 3
                            clickType.value = 0


                            null
                        }else if(clickType.value!=6){
                            showRefreshBtn = true
                            changeCreatedClickMode = 0

                            val data = habitCalendarValues(
                                dayOfYear,year,dayOfMonth
                                ,isMissedDay = !isChecked,
                                isCheckedDay = isChecked,
                                isHighlightedDay = false
                            )

                            when(clickType.value){
                                0,1->{

                                    val beforeCheck = hm.value[it.first]

                                    viewModel.toggleHabit(habit.toEntity(),context,All = null,
                                        dayOfYear = dayOfYear, year = year,markCheckedWhenIsEmpty=true,addHabitCheckNote
                                    =beforeCheck?.highlightedExplain?.takeIf { highlightedExplain!=it })

                                    clickType.value = if(isChecked) 0 else 1


                                }
                                2->{
                                    val beforeCheckExp = hm.value[it.first]?.highlightedExplain


                                    if(beforeCheckExp!=null && beforeCheckExp.trim()!="" && beforeCheckExp!=highlightedExplain){

                                    }else{
                                        habitCheckViewModel.deleteCheck(habitId,dayOfYear, year)
                                    }

                                    data.isCheckedDay = false
                                    data.isMissedDay = false
                                    data.isHighlightedDay = false
                                }
                                6->{
                                    data.isCheckedDay = !data.isCheckedDay
                                    data.isMissedDay = !data.isMissedDay



                                }
                                else->{
                                    data.isCheckedDay = !data.isCheckedDay
                                    data.isMissedDay = !data.isMissedDay
                                }
                            }


                            data
                        }else{

                            addNoteDialog = dayOfYear to year
                            null
                        }
                    }else{
                        null
                    }



                }
            }

            setHabitCalendar()



            var completationData by remember { mutableStateOf(listOf<Pair<String, Int>>()) }
            var completationDataForStreakAnalyzer by remember { mutableStateOf(HashMap<Pair<Int, Int>,Boolean?>()) }
            var timePeriod by remember { mutableStateOf(CompletionGraphOptions.first()) }
            var offsetMonth by remember { mutableStateOf(6) }//kaç ay öncesi ile şu anki zamanın istatistiğini gösterir


            CompletionGraph(completationData,completationDataForStreakAnalyzer,timePeriod, onTimePeriodChange = {
                timePeriod = it
            },onChangeMonthRangeChange={
                offsetMonth = it
            },selectedOptionMonthCount = offsetMonth,color = habit.color?.let{Color(it)}?: MaterialTheme.colorScheme.onSecondary)



            LaunchedEffect(Unit) {
                delay(1500)
                offsetMonth = 3
            }

            LaunchedEffect(habit,timePeriod,offsetMonth) {

                when(timePeriod){
                    CompletionGraphOptions[0]->{
                        //Günlük


                        val habitStartCalendar = Calendar.getInstance().apply {
                            timeInMillis = habit.createdAt
                        }

                        val todayCalendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                        }
                        val habitStartYear = habitStartCalendar.get(Calendar.YEAR)
                        val habitStartMonth = habitStartCalendar.get(Calendar.MONTH)
                        val habitStartDayOfMonth = habitStartCalendar.get(Calendar.DAY_OF_MONTH)
                        val habitDayOfWeek = if (habitStartCalendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                        else habitStartCalendar.get(Calendar.DAY_OF_WEEK) - 1

                        val exceptedDays = getExceptedDays(habit)
                        var checkCount = checkedCount


                        val dayOfYearHm = mutableListOf<Pair<Int, Int>>()
                        val names = mutableListOf<String>()



                        var addWeekDayName = habit.frequency == HabitFrequency.WEEKLY
                        var weekDays = habit.days.takeIf { habit.frequency == HabitFrequency.WEEKLY }
                        var monthDays = habit.days.takeIf { habit.frequency == HabitFrequency.MONTHLY }


                        suspend fun load( cYear: Int,cMonth: Int,startDayOfMonth: Int?=null,endDayOfMonth: Int?=null){



                            val startCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR,cYear)
                                set(Calendar.MONTH,cMonth)
                                set(Calendar.DAY_OF_MONTH,startDayOfMonth?:1)
                            }


                            val maxDayOfMonth = endDayOfMonth?: startCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                            while (startCal.get(Calendar.DAY_OF_MONTH)<=maxDayOfMonth && startCal.get(Calendar.MONTH) == cMonth) {


                                val dayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
                                val year = startCal.get(Calendar.YEAR)
                                val dayOfMonth = startCal.get(Calendar.DAY_OF_MONTH)

                                if((weekDays==null && monthDays==null) || monthDays?.contains(dayOfMonth)==true || weekDays?.contains(
                                        (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                        else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                            it
                                        }
                                )==true){
                                    dayOfYearHm.add(dayOfYear to year)
                                    names += "$dayOfMonth ${monthNames[cMonth]} ${if(addWeekDayName) " ${
                                        daysOfWeek[
                                            (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                            else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                                it-1
                                            }
                                        ]
                                    }" else ""}"
                                }




                                startCal.add(Calendar.DAY_OF_MONTH,1)
                            }




                            val notInDayOfYears = dayOfYearHm.map { it }
                                .filterNot { dayOfYear -> hm.value.keys.any { it == dayOfYear } }
                            if(notInDayOfYears.isNotEmpty()){
                                val data = habitCheckViewModel.getChecksForDays(habitId, dayOfYears = notInDayOfYears.map { it.first },
                                    years = notInDayOfYears.map{it.second})

                                data.let {


                                    val newHm = hm.value

                                    it.filterNotNull().forEach {
                                        val dayOfYear = it.dayOfYear
                                        val isHighlightedDay = dayOfYear == habit.longestStreakDayOfYear && it.year == habit.longestStreakYear
                                        newHm[dayOfYear to it.year] = habitCalendarValues(
                                            dayOfYear = dayOfYear,
                                            year = it.year,
                                            isMissedDay =  !it.isChecked,
                                            isCheckedDay =  it.isChecked,
                                            isHighlightedDay = isHighlightedDay,
                                            highlightedExplain =(highlightedExplain.takeIf { isHighlightedDay })
                                        )


                                    }

                                    hm.value = newHm
                                }
                            }
                        }



                        val monthRange = getMonthRange(habitStartCalendar,todayCalendar,offsetMonth)


                        var hhh = -1
                        fun calculateHabitStartDayOfMonth(cMonth: Int):Int{
                            if( hhh==-1){
                                hhh = when(habit.frequency){
                                    HabitFrequency.WEEKLY->{
                                        getFirstWeekdayOfMonth(
                                            habit.days.filter { it>=habitDayOfWeek }.let{
                                                if(it.isNotEmpty()) it.min() else habit.days.min()
                                            }
                                            ,habitStartYear,habitStartMonth
                                        ,startDayOfMonth=habitStartDayOfMonth.takeIf { cMonth == habitStartMonth }?:1)
                                    }
                                    HabitFrequency.MONTHLY->{
                                        habit.days.let{
                                            if(cMonth == habitStartMonth) it.filter{it<=habitStartDayOfMonth}
                                            else it
                                        }.min()
                                    }
                                    else->{
                                        if(habitStartMonth==cMonth){
                                            habitStartDayOfMonth
                                        }else{
                                            1
                                        }
                                    }
                                }
                            }
                            return hhh
                        }

                        for(range in monthRange){
                            val (cyear, cMonthIndex) = range

                            if(cyear>=habitStartYear && cyear<=year){

                                if(year == cyear && cyear == habitStartYear){
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                                                                                                    },
                                        endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }else if(year>cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex)

                                }else if(year>cyear && cyear==habitStartYear){
                                    //year == habitStartYear
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                    })

                                }else if(year==cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex ,endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }

                            }

                        }


                        val before = listOf<Pair<String, Int>>().toMutableList()
                        val before2 = HashMap<Pair<Int, Int>,Boolean?>()

                        var status:Boolean? = null
                        var statusControl = 0
                        var checkOffset = dayOfYearHm.count { hm.value[it]?.isCheckedDay == true }

                        for(i in dayOfYearHm.indices){
                            if(names.size >= dayOfYearHm.size ){
                                var name = names.getOrNull(i)?: ""
                                val pair = dayOfYearHm[i]
                                val cDayOfYear = pair.first
                                val cYear = pair.second
                                val data = hm.value[pair]





                                val cOffSet = if(habit.frequency == HabitFrequency.MONTHLY){

                                    countMatchingDays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetDays = habit.days
                                    )
                                }else if(habit.frequency == HabitFrequency.WEEKLY){
                                    countMatchingWeekdays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetWeekdays = habit.days
                                    )
                                }else{
                                    (if(year == cYear)
                                        dayOfYear - cDayOfYear  else {
                                        val currentDate = LocalDate.ofYearDay(cYear, cDayOfYear)
                                        val targetDate = LocalDate.ofYearDay(year, dayOfYear)

                                        ChronoUnit.DAYS.between(currentDate, targetDate).toInt()
                                    } )
                                }

                                var newExceptedDays = exceptedDays-cOffSet+ (1 .takeIf { habit.frequency==HabitFrequency.WEEKLY }?:0)

                                if(habit.frequency == HabitFrequency.MONTHLY){
                                    newExceptedDays++
                                  /*  val cMonthIndex = Calendar.getInstance().apply {
                                        set(Calendar.DAY_OF_YEAR,cDayOfYear)
                                        set(Calendar.YEAR,cYear)
                                    }.get(Calendar.MONTH)

                                    if(cMonthIndex==habitStartMonth && habitStartYear == cYear){
                                        newExceptedDays-=habit.days.size - habit.days.filter { it>=habitStartDayOfMonth }.size
                                        newExceptedDays++
                                    }*/

                                }



                                /*if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkCount++
                                } else checkCount=(checkCount-1).takeIf {  it>-1 }.let{
                                    status = null
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl = (statusControl-1).takeIf { it>-1 }?: 0
                                    }
                                    if(it!=null) it else{
                                        status = false.takeIf { (data!=null)  }
                                        statusControl = 0
                                        0
                                    }
                                }*/

                                if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkOffset--
                                } else {
                                    status = false.takeIf { (data!=null)  }
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl--
                                    }
                                }

                                val checkCount = checkedCount - checkOffset

                                val percentage = if (newExceptedDays > 0) {
                                    ((checkCount.toFloat() / newExceptedDays.toFloat()) * 100).toInt()
                                } else 0

                                val dtxt = (newExceptedDays.takeIf { it<300 }?.toString()?.let{"$it. Gün"}?: name)

                                val popUpText =
                                    "$dtxt\n$checkCount/$newExceptedDays gün Yapıldı.\nPuan: $percentage%${name.takeIf { dtxt!=it }?.let{"\n\n\n$it"}?:""}"

                                before.add("$popUpText-|-${status?.let{ if(it) "1|" else "0|" }?: "|"}$name" to (percentage) )
                                before2[cDayOfYear to cYear] = when {
                                    data?.isCheckedDay == true -> true
                                    data?.isMissedDay == true -> false
                                    else -> null
                                }


                            }

                        }


                        completationData = before
                        completationDataForStreakAnalyzer = before2

                    }
                    CompletionGraphOptions[1]->{
                        //Haftalık



                        val habitStartCalendar = Calendar.getInstance().apply {
                            timeInMillis = habit.createdAt
                        }

                        val todayCalendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                        }
                        val habitStartYear = habitStartCalendar.get(Calendar.YEAR)
                        val habitStartMonth = habitStartCalendar.get(Calendar.MONTH)
                        val habitStartDayOfMonth = habitStartCalendar.get(Calendar.DAY_OF_MONTH)
                        val habitDayOfWeek = if (habitStartCalendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                        else habitStartCalendar.get(Calendar.DAY_OF_WEEK) - 1

                        val exceptedDays = getExceptedDays(habit)
                        var checkCount = checkedCount


                        val dayOfYearHm = mutableListOf<Pair<Int, Int>>()
                        val names = mutableListOf<String>()



                        var addWeekDayName = habit.frequency == HabitFrequency.WEEKLY
                        var weekDays = habit.days.takeIf { habit.frequency == HabitFrequency.WEEKLY }
                        var monthDays = habit.days.takeIf { habit.frequency == HabitFrequency.MONTHLY }


                        suspend fun load( cYear: Int,cMonth: Int,startDayOfMonth: Int?=null,endDayOfMonth: Int?=null){



                            val startCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR,cYear)
                                set(Calendar.MONTH,cMonth)
                                set(Calendar.DAY_OF_MONTH,startDayOfMonth?:1)
                            }


                            val maxDayOfMonth = endDayOfMonth?: startCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                            while (startCal.get(Calendar.DAY_OF_MONTH)<=maxDayOfMonth && startCal.get(Calendar.MONTH) == cMonth) {


                                val dayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
                                val year = startCal.get(Calendar.YEAR)
                                val dayOfMonth = startCal.get(Calendar.DAY_OF_MONTH)

                                if((weekDays==null && monthDays==null) || monthDays?.contains(dayOfMonth)==true || weekDays?.contains(
                                        (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                        else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                            it
                                        }
                                    )==true){
                                    dayOfYearHm.add(dayOfYear to year)
                                    names += "$dayOfMonth ${monthNames[cMonth]} ${if(addWeekDayName) " ${
                                        daysOfWeek[
                                            (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                            else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                                it-1
                                            }
                                        ]
                                    }" else ""}"
                                }




                                startCal.add(Calendar.DAY_OF_MONTH,1)
                            }




                            val notInDayOfYears = dayOfYearHm.map { it }
                                .filterNot { dayOfYear -> hm.value.keys.any { it == dayOfYear } }
                            if(notInDayOfYears.isNotEmpty()){
                                val data = habitCheckViewModel.getChecksForDays(habitId, dayOfYears = notInDayOfYears.map { it.first },
                                    years = notInDayOfYears.map{it.second})

                                data.let {


                                    val newHm = hm.value

                                    it.filterNotNull().forEach {
                                        val dayOfYear = it.dayOfYear
                                        val isHighlightedDay = dayOfYear == habit.longestStreakDayOfYear && it.year == habit.longestStreakYear
                                        newHm[dayOfYear to it.year] = habitCalendarValues(
                                            dayOfYear = dayOfYear,
                                            year = it.year,
                                            isMissedDay =  !it.isChecked,
                                            isCheckedDay =  it.isChecked,
                                            isHighlightedDay = isHighlightedDay,
                                            highlightedExplain =(highlightedExplain.takeIf { isHighlightedDay })
                                        )


                                    }

                                    hm.value = newHm
                                }
                            }
                        }



                        val monthRange = getMonthRange(habitStartCalendar,todayCalendar,offsetMonth)


                        var hhh = -1
                        fun calculateHabitStartDayOfMonth(cMonth: Int):Int{
                            if( hhh==-1){
                                hhh = when(habit.frequency){
                                    HabitFrequency.WEEKLY->{
                                        getFirstWeekdayOfMonth(
                                            habit.days.filter { it>=habitDayOfWeek }.let{
                                                if(it.isNotEmpty()) it.min() else habit.days.min()
                                            }
                                            ,habitStartYear,habitStartMonth
                                            ,startDayOfMonth=habitStartDayOfMonth.takeIf { cMonth == habitStartMonth }?:1)
                                    }
                                    HabitFrequency.MONTHLY->{
                                        habit.days.let{
                                            if(cMonth == habitStartMonth) it.filter{it<=habitStartDayOfMonth}
                                            else it
                                        }.min()
                                    }
                                    else->{
                                        if(habitStartMonth==cMonth){
                                            habitStartDayOfMonth
                                        }else{
                                            1
                                        }
                                    }
                                }
                            }
                            return hhh
                        }

                        for(range in monthRange){
                            val (cyear, cMonthIndex) = range

                            if(cyear>=habitStartYear && cyear<=year){

                                if(year == cyear && cyear == habitStartYear){
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                    },
                                        endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }else if(year>cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex)

                                }else if(year>cyear && cyear==habitStartYear){
                                    //year == habitStartYear
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                    })

                                }else if(year==cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex ,endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }

                            }

                        }


                        val before = listOf<Pair<String, Int>>().toMutableList()
                        val before2 = HashMap<Pair<Int, Int>,Boolean?>()

                        var status:Boolean? = null
                        var statusControl = 0
                        var checkOffset = dayOfYearHm.count { hm.value[it]?.isCheckedDay == true }



                        var dayOfYearHm2 = dayOfYearHm.toMutableList()
                        var names2 = names.toMutableList()

                        val newL = dayOfYearHm2.toMutableList()
                        var control = false
                        dayOfYearHm2.forEach {
                            if(!control){
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.DAY_OF_YEAR,it.first)
                                    set(Calendar.YEAR,it.second)
                                }.get(Calendar.DAY_OF_WEEK)

                                if(cal>1){
                                    control = true
                                }else{
                                    hm.value[it]?.let{
                                        if(it.isCheckedDay){
                                            checkOffset--
                                        }
                                    }
                                }
                            }
                            if(control){
                                newL.add(it)
                            }
                        }
                        dayOfYearHm2 = newL


                        data class WeeklyGroup(
                            val startOfWeekDayOfYear: Int,
                            val startOfWeekYear: Int,
                            val days: List<Pair<Int, Int>>
                        )

                        fun groupByWeeksWithStartDate(dayOfYearList: List<Pair<Int, Int>>): List<WeeklyGroup> {
                            val grouped = mutableListOf<WeeklyGroup>()

                            val sortedList = dayOfYearList.sortedWith(compareBy({ it.second }, { it.first }))
                            if (sortedList.isEmpty()) return grouped

                            val calendar = Calendar.getInstance()
                            var currentWeek = mutableListOf<Pair<Int, Int>>()
                            var lastWeekOfYear = -1
                            var lastYear = -1
                            var weekStartDayOfYear = -1
                            var weekStartYear = -1

                            for ((dayOfYear, year) in sortedList) {
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
                                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

                                if (weekOfYear != lastWeekOfYear || year != lastYear) {
                                    if (currentWeek.isNotEmpty()) {
                                        grouped.add(
                                            WeeklyGroup(
                                                startOfWeekDayOfYear = weekStartDayOfYear,
                                                startOfWeekYear = weekStartYear,
                                                days = currentWeek.toList()
                                            )
                                        )
                                        currentWeek.clear()
                                    }

                                    // yeni haftanın başlangıcı
                                    lastWeekOfYear = weekOfYear
                                    lastYear = year

                                    calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                                    weekStartDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                                    weekStartYear = calendar.get(Calendar.YEAR)
                                }

                                currentWeek.add(dayOfYear to year)
                            }

                            if (currentWeek.isNotEmpty()) {
                                grouped.add(
                                    WeeklyGroup(
                                        startOfWeekDayOfYear = weekStartDayOfYear,
                                        startOfWeekYear = weekStartYear,
                                        days = currentWeek.toList()
                                    )
                                )
                            }

                            return grouped
                        }

                        dayOfYearHm2.sortWith(compareBy({ it.second }, { it.first }))
                        val groupedWeeks = groupByWeeksWithStartDate(dayOfYearHm2)


                        for(i in groupedWeeks){

                            val cal = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_YEAR,i.startOfWeekDayOfYear)
                                set(Calendar.YEAR,i.startOfWeekYear)
                            }

                            val sDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                            val sMonth = cal.get(Calendar.MONTH).let{
                                monthNames[it].take(3)
                            }


                            var name = "${sDayOfMonth} ${sMonth}"


                            val cDayOfYear = i.days.first().first
                            var newExceptedDays = exceptedDays
                            val data = hm.value[i.days.first()]
                            val cYear = i.days.first().second
                            i.days.forEach { j->
                                val pair = j
                                val cDayOfYear = pair.first
                                val cYear = pair.second
                                val data = hm.value[pair]


                                val cOffSet = if(habit.frequency == HabitFrequency.MONTHLY){

                                    countMatchingDays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetDays = habit.days
                                    )
                                }else if(habit.frequency == HabitFrequency.WEEKLY){
                                    countMatchingWeekdays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetWeekdays = habit.days
                                    )
                                }else{
                                    (if(year == cYear)
                                        dayOfYear - cDayOfYear  else {
                                        val currentDate = LocalDate.ofYearDay(cYear, cDayOfYear)
                                        val targetDate = LocalDate.ofYearDay(year, dayOfYear)

                                        ChronoUnit.DAYS.between(currentDate, targetDate).toInt()
                                    } )
                                }

                                var newExceptedDays = exceptedDays-cOffSet+ (1 .takeIf { habit.frequency==HabitFrequency.WEEKLY }?:0)

                                if(habit.frequency == HabitFrequency.MONTHLY){
                                    newExceptedDays++
                                    /*  val cMonthIndex = Calendar.getInstance().apply {
                                          set(Calendar.DAY_OF_YEAR,cDayOfYear)
                                          set(Calendar.YEAR,cYear)
                                      }.get(Calendar.MONTH)

                                      if(cMonthIndex==habitStartMonth && habitStartYear == cYear){
                                          newExceptedDays-=habit.days.size - habit.days.filter { it>=habitStartDayOfMonth }.size
                                          newExceptedDays++
                                      }*/

                                }



                                /*if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkCount++
                                } else checkCount=(checkCount-1).takeIf {  it>-1 }.let{
                                    status = null
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl = (statusControl-1).takeIf { it>-1 }?: 0
                                    }
                                    if(it!=null) it else{
                                        status = false.takeIf { (data!=null)  }
                                        statusControl = 0
                                        0
                                    }
                                }*/


                                if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkOffset--
                                } else {
                                    status = false.takeIf { (data!=null)  }
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl--
                                    }
                                }

                            }

                            val checkCount = (checkedCount - checkOffset)/2


                            val percentage = if (newExceptedDays > 0) {
                                ((checkCount.toFloat() / newExceptedDays.toFloat()) * 100).toInt()
                            } else 0

                            val dtxt = (newExceptedDays.takeIf { it<300 }?.toString()?.let{"$it. Gün"}?: name)

                            val popUpText =
                                "$dtxt\n$checkCount/$newExceptedDays gün Yapıldı.\nPuan: $percentage%${name.takeIf { dtxt!=it }?.let{"\n\n\n$it"}?:""}"

                            before.add("$popUpText-|-${status?.let{ if(it) "1|" else "0|" }?: "|"}$name" to (percentage) )
                            before2[cDayOfYear to cYear] = when {
                                data?.isCheckedDay == true -> true
                                data?.isMissedDay == true -> false
                                else -> null
                            }



                        }


                        completationData = before
                        completationDataForStreakAnalyzer = before2
                    }
                    else->{
                        //Aylık




                        val habitStartCalendar = Calendar.getInstance().apply {
                            timeInMillis = habit.createdAt
                        }

                        val todayCalendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                        }
                        val habitStartYear = habitStartCalendar.get(Calendar.YEAR)
                        val habitStartMonth = habitStartCalendar.get(Calendar.MONTH)
                        val habitStartDayOfMonth = habitStartCalendar.get(Calendar.DAY_OF_MONTH)
                        val habitDayOfWeek = if (habitStartCalendar.get(Calendar.DAY_OF_WEEK) == 1) 7
                        else habitStartCalendar.get(Calendar.DAY_OF_WEEK) - 1

                        val exceptedDays = getExceptedDays(habit)
                        var checkCount = checkedCount


                        val dayOfYearHm = mutableListOf<Pair<Int, Int>>()
                        val names = mutableListOf<String>()



                        var addWeekDayName = habit.frequency == HabitFrequency.WEEKLY
                        var weekDays = habit.days.takeIf { habit.frequency == HabitFrequency.WEEKLY }
                        var monthDays = habit.days.takeIf { habit.frequency == HabitFrequency.MONTHLY }


                        suspend fun load( cYear: Int,cMonth: Int,startDayOfMonth: Int?=null,endDayOfMonth: Int?=null){



                            val startCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR,cYear)
                                set(Calendar.MONTH,cMonth)
                                set(Calendar.DAY_OF_MONTH,startDayOfMonth?:1)
                            }


                            val maxDayOfMonth = endDayOfMonth?: startCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                            while (startCal.get(Calendar.DAY_OF_MONTH)<=maxDayOfMonth && startCal.get(Calendar.MONTH) == cMonth) {


                                val dayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
                                val year = startCal.get(Calendar.YEAR)
                                val dayOfMonth = startCal.get(Calendar.DAY_OF_MONTH)

                                if((weekDays==null && monthDays==null) || monthDays?.contains(dayOfMonth)==true || weekDays?.contains(
                                        (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                        else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                            it
                                        }
                                    )==true){
                                    dayOfYearHm.add(dayOfYear to year)
                                    names += "$dayOfMonth ${monthNames[cMonth]} ${if(addWeekDayName) " ${
                                        daysOfWeek[
                                            (if (startCal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 7
                                            else startCal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let{
                                                it-1
                                            }
                                        ]
                                    }" else ""}"
                                }




                                startCal.add(Calendar.DAY_OF_MONTH,1)
                            }




                            val notInDayOfYears = dayOfYearHm.map { it }
                                .filterNot { dayOfYear -> hm.value.keys.any { it == dayOfYear } }
                            if(notInDayOfYears.isNotEmpty()){
                                val data = habitCheckViewModel.getChecksForDays(habitId, dayOfYears = notInDayOfYears.map { it.first },
                                    years = notInDayOfYears.map{it.second})

                                data.let {


                                    val newHm = hm.value

                                    it.filterNotNull().forEach {
                                        val dayOfYear = it.dayOfYear
                                        val isHighlightedDay = dayOfYear == habit.longestStreakDayOfYear && it.year == habit.longestStreakYear
                                        newHm[dayOfYear to it.year] = habitCalendarValues(
                                            dayOfYear = dayOfYear,
                                            year = it.year,
                                            isMissedDay =  !it.isChecked,
                                            isCheckedDay =  it.isChecked,
                                            isHighlightedDay = isHighlightedDay,
                                            highlightedExplain =(highlightedExplain.takeIf { isHighlightedDay })
                                        )


                                    }

                                    hm.value = newHm
                                }
                            }
                        }



                        val monthRange = getMonthRange(habitStartCalendar,todayCalendar,offsetMonth)


                        var hhh = -1
                        fun calculateHabitStartDayOfMonth(cMonth: Int):Int{
                            if( hhh==-1){
                                hhh = when(habit.frequency){
                                    HabitFrequency.WEEKLY->{
                                        getFirstWeekdayOfMonth(
                                            habit.days.filter { it>=habitDayOfWeek }.let{
                                                if(it.isNotEmpty()) it.min() else habit.days.min()
                                            }
                                            ,habitStartYear,habitStartMonth
                                            ,startDayOfMonth=habitStartDayOfMonth.takeIf { cMonth == habitStartMonth }?:1)
                                    }
                                    HabitFrequency.MONTHLY->{
                                        habit.days.let{
                                            if(cMonth == habitStartMonth) it.filter{it<=habitStartDayOfMonth}
                                            else it
                                        }.min()
                                    }
                                    else->{
                                        if(habitStartMonth==cMonth){
                                            habitStartDayOfMonth
                                        }else{
                                            1
                                        }
                                    }
                                }
                            }
                            return hhh
                        }

                        for(range in monthRange){
                            val (cyear, cMonthIndex) = range

                            if(cyear>=habitStartYear && cyear<=year){

                                if(year == cyear && cyear == habitStartYear){
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                    },
                                        endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }else if(year>cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex)

                                }else if(year>cyear && cyear==habitStartYear){
                                    //year == habitStartYear
                                    load(cyear,cMonthIndex,startDayOfMonth = calculateHabitStartDayOfMonth(cMonthIndex).takeIf {
                                        cMonthIndex == habitStartMonth
                                    })

                                }else if(year==cyear && cyear>habitStartYear){
                                    load(cyear,cMonthIndex ,endDayOfMonth = dayOfMonth.takeIf { cMonthIndex == monthIndex})
                                }

                            }

                        }


                        val before = listOf<Pair<String, Int>>().toMutableList()
                        val before2 = HashMap<Pair<Int, Int>,Boolean?>()

                        var status:Boolean? = null
                        var statusControl = 0
                        var checkOffset = dayOfYearHm.count { hm.value[it]?.isCheckedDay == true }



                        var dayOfYearHm2 = dayOfYearHm.toMutableList()
                        var names2 = names.toMutableList()

                        val newL = dayOfYearHm2.toMutableList()
                        var control = false
                        dayOfYearHm2 = newL



                        data class MonthlyGroup(
                            val startOfMonthDayOfYear: Int,
                            val startOfMonthYear: Int,
                            val days: List<Pair<Int, Int>>
                        )

                        fun groupByMonthsWithStartDate(dayOfYearList: List<Pair<Int, Int>>): List<MonthlyGroup> {
                            val grouped = mutableListOf<MonthlyGroup>()

                            val sortedList = dayOfYearList.sortedWith(compareBy({ it.second }, { it.first }))
                            if (sortedList.isEmpty()) return grouped

                            val calendar = Calendar.getInstance()
                            var currentMonth = mutableListOf<Pair<Int, Int>>()
                            var lastMonth = -1
                            var lastYear = -1
                            var monthStartDayOfYear = -1
                            var monthStartYear = -1

                            for ((dayOfYear, year) in sortedList) {
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
                                val month = calendar.get(Calendar.MONTH)

                                if (month != lastMonth || year != lastYear) {
                                    if (currentMonth.isNotEmpty()) {
                                        grouped.add(
                                            MonthlyGroup(
                                                startOfMonthDayOfYear = monthStartDayOfYear,
                                                startOfMonthYear = monthStartYear,
                                                days = currentMonth.toList()
                                            )
                                        )
                                        currentMonth.clear()
                                    }

                                    lastMonth = month
                                    lastYear = year

                                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                                    monthStartDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                                    monthStartYear = calendar.get(Calendar.YEAR)
                                }

                                currentMonth.add(dayOfYear to year)
                            }

                            if (currentMonth.isNotEmpty()) {
                                grouped.add(
                                    MonthlyGroup(
                                        startOfMonthDayOfYear = monthStartDayOfYear,
                                        startOfMonthYear = monthStartYear,
                                        days = currentMonth.toList()
                                    )
                                )
                            }

                            return grouped
                        }

                        dayOfYearHm2.sortWith(compareBy({ it.second }, { it.first }))
                        val groupedWeeks = groupByMonthsWithStartDate(dayOfYearHm2)


                        for(i in groupedWeeks){

                            val cal = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_YEAR,i.startOfMonthDayOfYear)
                                set(Calendar.YEAR,i.startOfMonthYear)
                            }

                            val sDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                            val sMonth = cal.get(Calendar.MONTH).let{
                                monthNames[it].take(3)
                            }


                            var name = "${sDayOfMonth} ${sMonth}"


                            val cDayOfYear = i.days.first().first
                            var newExceptedDays = exceptedDays
                            val data = hm.value[i.days.first()]
                            val cYear = i.days.first().second
                            i.days.forEach { j->
                                val pair = j
                                val cDayOfYear = pair.first
                                val cYear = pair.second
                                val data = hm.value[pair]


                                val cOffSet = if(habit.frequency == HabitFrequency.MONTHLY){

                                    countMatchingDays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetDays = habit.days
                                    )
                                }else if(habit.frequency == HabitFrequency.WEEKLY){
                                    countMatchingWeekdays(
                                        startDayOfYear = cDayOfYear,
                                        startYear = cYear,
                                        endDayOfYear = dayOfYear,
                                        endYear = year,
                                        targetWeekdays = habit.days
                                    )
                                }else{
                                    (if(year == cYear)
                                        dayOfYear - cDayOfYear  else {
                                        val currentDate = LocalDate.ofYearDay(cYear, cDayOfYear)
                                        val targetDate = LocalDate.ofYearDay(year, dayOfYear)

                                        ChronoUnit.DAYS.between(currentDate, targetDate).toInt()
                                    } )
                                }

                                var newExceptedDays = exceptedDays-cOffSet+ (1 .takeIf { habit.frequency==HabitFrequency.WEEKLY }?:0)

                                if(habit.frequency == HabitFrequency.MONTHLY){
                                    newExceptedDays++
                                    /*  val cMonthIndex = Calendar.getInstance().apply {
                                          set(Calendar.DAY_OF_YEAR,cDayOfYear)
                                          set(Calendar.YEAR,cYear)
                                      }.get(Calendar.MONTH)

                                      if(cMonthIndex==habitStartMonth && habitStartYear == cYear){
                                          newExceptedDays-=habit.days.size - habit.days.filter { it>=habitStartDayOfMonth }.size
                                          newExceptedDays++
                                      }*/

                                }



                                /*if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkCount++
                                } else checkCount=(checkCount-1).takeIf {  it>-1 }.let{
                                    status = null
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl = (statusControl-1).takeIf { it>-1 }?: 0
                                    }
                                    if(it!=null) it else{
                                        status = false.takeIf { (data!=null)  }
                                        statusControl = 0
                                        0
                                    }
                                }*/


                                if(data?.isCheckedDay==true) {
                                    status = true
                                    statusControl = 0
                                    checkOffset--
                                } else {
                                    status = false.takeIf { (data!=null)  }
                                    statusControl++
                                    if(statusControl>=3){
                                        status = false
                                        statusControl--
                                    }
                                }

                            }

                            val checkCount = (checkedCount - checkOffset)


                            val percentage = if (newExceptedDays > 0) {
                                ((checkCount.toFloat() / newExceptedDays.toFloat()) * 100).toInt()
                            } else 0

                            val dtxt = (newExceptedDays.takeIf { it<300 }?.toString()?.let{"$it. Gün"}?: name)

                            val popUpText =
                                "$dtxt\n$checkCount/$newExceptedDays gün Yapıldı.\nPuan: $percentage%${name.takeIf { dtxt!=it }?.let{"\n\n\n$it"}?:""}"

                            before.add("$popUpText-|-${status?.let{ if(it) "1|" else "0|" }?: "|"}$name" to (percentage) )
                            before2[cDayOfYear to cYear] = when {
                                data?.isCheckedDay == true -> true
                                data?.isMissedDay == true -> false
                                else -> null
                            }



                        }


                        completationData = before
                        completationDataForStreakAnalyzer = before2
                    }
                }

            }

            Spacer(Modifier.height(40.dp))



            var dayOfYearOffsetForHourlyTrendChart = remember { mutableStateOf(90) }
            suspend fun calcTimes():List<Pair<Float, Float>>{

                val cal = Calendar.getInstance().apply {
                    timeInMillis =System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR,-dayOfYearOffsetForHourlyTrendChart.value)
                }


                val data = habitCheckViewModel.getChecksFromDateRange(habitId,cal.get(Calendar.YEAR),year
                    ,cal.get(Calendar.DAY_OF_YEAR),

                    dayOfYear)

                val filteredData = data?.filter { it?.isChecked==true }?.filterNotNull()

                return (filteredData?.sortedWith (compareBy({ it.year }, { it.dayOfYear }) )?.map { it.time.let{ getHourAndMinuteFromTimestamp(it).let{it.first.toFloat() to it.second.toFloat()} } }?: emptyList())
            }

            val newHourlyTrendChartDatas = remember { mutableStateOf<List<Pair<Float, Float>>>(
                emptyList()
            ) }

            LaunchedEffect(dayOfYearOffsetForHourlyTrendChart.value) {
                newHourlyTrendChartDatas.value = calcTimes()
            }


            if(newHourlyTrendChartDatas.value.isNotEmpty()){

                Column(Modifier.fillMaxWidth().height(300.dp).padding(horizontal = 40.dp, vertical = 15.dp)) {
                    HourlyTrendChart(newHourlyTrendChartDatas.value.reversed()){
                        dayOfYearOffsetForHourlyTrendChart.value = it
                    }
                }
            }


            Spacer(Modifier.height(60.dp))



            var dayOfYearOffsetForWeeklyFrequencyChart = remember { mutableStateOf(90) }
            val newWeeklyFrequencyChartDatas = remember { mutableStateOf<List<Pair<Pair<Int,Int>,List<Int>>>>(
                emptyList()
            ) }
            suspend fun getWeeklyDatas():List<Pair<Pair<Int,Int>,List<Int>>>{


                val dayOfYearHm = mutableListOf<Pair<Int, Int>>()
                Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR,-dayOfYearOffsetForWeeklyFrequencyChart.value)
                }.let{startCal->
                    while (startCal.timeInMillis<=System.currentTimeMillis()){

                        val dayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
                        val year = startCal.get(Calendar.YEAR)

                        dayOfYearHm.add(dayOfYear to year)

                        startCal.add(Calendar.DAY_OF_YEAR,1)
                    }
                }
                val notInDayOfYears = dayOfYearHm.map { it }
                    .filterNot { dayOfYear -> hm.value.keys.any { it == dayOfYear } }
                if(notInDayOfYears.isNotEmpty()){
                    val data = habitCheckViewModel.getChecksForDays(habitId, dayOfYears = notInDayOfYears.map { it.first },
                        years = notInDayOfYears.map{it.second})

                    data.let {


                        val newHm = hm.value

                        it.filterNotNull().forEach {
                            val dayOfYear = it.dayOfYear
                            val isHighlightedDay = dayOfYear == habit.longestStreakDayOfYear && it.year == habit.longestStreakYear
                            newHm[dayOfYear to it.year] = habitCalendarValues(
                                dayOfYear = dayOfYear,
                                year = it.year,
                                isMissedDay =  !it.isChecked,
                                isCheckedDay =  it.isChecked,
                                isHighlightedDay = isHighlightedDay,
                                highlightedExplain =(highlightedExplain.takeIf { isHighlightedDay })
                            )


                        }

                        hm.value = newHm
                    }
                }



                val list = mutableListOf<Pair<Pair<Int,Int>,MutableList<Int>>>()


                val startCal = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR,-dayOfYearOffsetForWeeklyFrequencyChart.value)
                }


                var currentMonthIndex = -1
                while (startCal.timeInMillis<=System.currentTimeMillis()){

                    val dayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
                    val year = startCal.get(Calendar.YEAR)
                    val month = startCal.get(Calendar.MONTH) + 1
                    val weekDay = startCal.get(Calendar.DAY_OF_WEEK).let{
                        if(it==1) 7 else it-1
                    }

                    if(currentMonthIndex==month){
                        list.last().second.let{
                            val data = hm.value[dayOfYear to year]?.isCheckedDay==true
                            if(data) it[weekDay-1]+=1
                        }
                    }else{
                        currentMonthIndex = month
                        list.add((month to year) to List(7){0}.toMutableList())
                    }

                    startCal.add(Calendar.DAY_OF_YEAR,1)
                }


                return list





            }
            LaunchedEffect(dayOfYearOffsetForWeeklyFrequencyChart.value) {
                newWeeklyFrequencyChartDatas.value = getWeeklyDatas()
            }

            if(newWeeklyFrequencyChartDatas.value.isNotEmpty()){

                Column(Modifier.fillMaxWidth().height(350.dp).padding(horizontal = 40.dp, vertical = 15.dp)) {
                    FrequencyChartCircleStyle(newWeeklyFrequencyChartDatas.value.reversed()){
                        dayOfYearOffsetForWeeklyFrequencyChart.value = it
                    }
                }
            }



            Spacer(Modifier.height(40.dp))


            Divider()






            Column (Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 45.dp
                ), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){

                AchievementDisplay(context,habitCheckViewModel, habit)

            }





            Spacer(Modifier.height(60.dp))



        }


    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            showColorDialog = true
            changeTaskColorViewModel.refreshDoneForHabit()
        }
    }
    LaunchedEffect(shouldRefreshEditDialog) {
        if (shouldRefreshEditDialog) {


            showLongClickDialog = true

            changeTaskColorViewModel.refreshDoneForHabitEditDialog()
        }
    }
    LaunchedEffect(shouldRefreshDelDialog) {
        if (shouldRefreshDelDialog) {


            showDelDialog = true

            changeTaskColorViewModel.refreshDoneForHabitDelDialog()
        }
    }
}



@Composable
fun label1(text1: String, color1:Color?=null,
           text2: String?=null, color2:Color?=null,
           text3: String?=null, color3:Color?=null,
           content: (@Composable RowScope.() -> Unit)? = null
){
    Row(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Start
    , verticalAlignment = Alignment.CenterVertically) {

        Box(Modifier
            .size(4.dp)
            .clip(CircleShape)){}

        Spacer(Modifier.width(4.dp))

        Text(text = text1, color = color1?: MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start)
        text2?.let { Text(text = it, color = color2?: MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start) }
        text3?.let { Text(text = it, color = color3?: MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Start) }


        content?.invoke(this)

    }
}


var randomDarkColor2: Color?=null
@Composable
fun AchievementDisplay(context: Context, habitCheckViewModel: HabitCheckViewModel, habit: Habit) {

    val achUtils = AchievementUtils()

    var randomDarkColor = randomDarkColor2?: getRandomDarkColor()

    LaunchedEffect(Unit) {
        randomDarkColor2 = randomDarkColor
    }

    val total = remember { mutableStateOf<Int>(0) }
    val achievement = remember { mutableStateOf<List<String>?>(null) }
    var showdiaByCat by remember { mutableStateOf<String?>(null) }
    var showdiaByCatOffsetValue by remember { mutableStateOf<String>("") }
    var addDia by remember { mutableStateOf<Boolean>(false) }

    val delAchName = remember { mutableStateOf<String?>(null) }
    if (delAchName.value!=null) {
        AlertDialog(
            onDismissRequest = {delAchName.value=null},
            title = {
                Text(
                    text = delAchName.value.toString(),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {
                Text(
                    text = "Sil?",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {


                    achUtils.delCustomToSp(context.getSharedPreferences(context.packageName,
                        MODE_PRIVATE),delAchName.value.toString())


                    delAchName.value=null
                    showdiaByCat = null

                }) {
                    Text(
                        text = "Evet",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {delAchName.value=null}) {
                    Text(
                        text = "Hayır",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        )
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
        Text("\n\n  Başarımlar ${if(total.value>0) "(${total.value}/${achUtils.achievements.size + achUtils.advancedAchievements.size + achUtils.customCount
        })" else ""}  \n\n", color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Center,
        )

        Image(Icons.Default.ArrowDropDown,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier.clickable {
                showdiaByCatOffsetValue = ""
                showdiaByCat = "null"
            })


        Spacer(Modifier.width(16.dp))


        Box(Modifier

            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray.copy(.7f))
            .clickable {

                addDia = true

            }
            , contentAlignment = Alignment.Center){

            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "  Ekle",
                    modifier = Modifier
                        .padding(4.dp)
                    ,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(Modifier.width(8.dp))

                Icon(Icons.Default.Add,"", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }




    if(showdiaByCat!=null){
        AlertDialog(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 150.dp),
            onDismissRequest = {
                showdiaByCat = null
            },
            title = {
                Text(
                    text = showdiaByCat.let{if(it=="null") "Başarımlar" else it}.toString(),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {
                achUtils.getAchievementList(showdiaByCat.let{if(it=="null") null else it}, sp = context
                    .getSharedPreferences(context.packageName, MODE_PRIVATE)).let {

                    var isUnclocked = true

                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()))  {
                        it.indices.forEach{index->
                            val item = it[index]

                            Spacer(Modifier.height(16.dp))
                            // Container Card
                            val name = item.name.substringBefore("|")
                            val description = item.description.substringBefore("|Özel").substringAfter("|").substringBefore("--")



                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if(isUnclocked) randomDarkColor else
                                        Color.Black.copy(0.3f)) // Dinamik koyu renkler
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                item.description.contains("|Özel").let{
                                                    if(it){
                                                        delAchName.value = name
                                                    }
                                                }
                                            },
                                            onTap = {
                                                Toast.makeText(context,description, Toast.LENGTH_SHORT).show()


                                            }
                                        )
                                    }

                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Başlık
                                    Text(
                                        text = name,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    // Açıklama
                                    Text(
                                        text = description,
                                        color = MaterialTheme.colorScheme.onSecondary.copy(0.7f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            if(showdiaByCatOffsetValue == name && !item.description.contains("|Özel") && false){
                                isUnclocked = false
                                Divider()
                            }
                        }
                    }

                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showdiaByCat = null

                }) {
                    Text(
                        text = "Tamam",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            },
            dismissButton = {

            }
        )
    }
    if(addDia){
        AlertDialog(
            modifier = Modifier.fillMaxSize(),
            onDismissRequest = {
                addDia = false
            },
            title = {
                Text(
                    text = "Başarım Ekle",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {

                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    AddAchievementScreen(context,achUtils){
                        addDia = false
                    }
                }


            },
            confirmButton = {
                TextButton(onClick = {
                    addDia = false


                }) {
                    Text(
                        text = "Tamam",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            },
            dismissButton = {

            }
        )
    }

    var openAchievement = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        habitCheckViewModel.getHabitAchievement(habit,achUtils,context.getSharedPreferences(context.packageName,
            MODE_PRIVATE)) {
            it?.let {
                achievement.value = it.map { it.second?.let{v-> it.second!!.name + "|" + it.second!!.description + "--" + it.first }}
                    .filterNotNull()
                total.value = achUtils.totalCompleted
            }
        }
    }

    if(!openAchievement.value){
        Text("\n\n\n\n\nBaşarımlar (Görüntüle)", color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier
            .clickable {
                openAchievement.value = true
            })
    }else{

        // Eğer başarı var ise, aşağıdaki blok çalışacak

        if (achievement.value != null) {

            Spacer(Modifier.height(16.dp))


            Column(Modifier.fillMaxWidth()) {

                achievement.value?.forEach{
                    val name = it.substringBefore("|")
                    val description = it.substringBefore("|Özel").substringAfter("|").substringBefore("--")
                    val cat = it.substringAfter("--")

                    Spacer(Modifier.height(16.dp))

                    Text(cat,Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary.copy(if(isLightTheme()) 1f else 0.6f),
                        textAlign = TextAlign.Start)

                    Spacer(Modifier.height(16.dp))


                    // Container Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(randomDarkColor) // Dinamik koyu renkler
                            .clickable {
                                showdiaByCatOffsetValue = name

                                showdiaByCat = cat

                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Başlık
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            // Açıklama
                            Text(
                                text = description.substringAfter("|"),
                                color = MaterialTheme.colorScheme.onSecondary.copy(0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

}

fun getRandomDarkColor(): Color {
    // Koyu tonlarda renk seçimi
    val darkColors = listOf(
        Color(0xFF2D2D2D), // Koyu gri
        Color(0xFF3A3A3A), // Hakkında koyu gri ton
        Color(0xFF4E4E4E), // Hakkında bir ton daha koyu
        Color(0xFF1A1A1A), // Neredeyse siyah
        Color(0xFF4A90E2), // Koyu mavi
        Color(0xFF9B59B6), // Koyu mor
        Color(0xFF34495E)  // Koyu lacivert
    )
    return darkColors.random() // Rastgele bir renk seçilir
}



@Composable
fun AddAchievementScreen(context: Context,achievementUtils: AchievementUtils, disMiss: ()->Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition_totalMarkedDays by remember { mutableStateOf("") }
    var condition_maxStreak by remember { mutableStateOf("") }
    var condition_currentStreak by remember { mutableStateOf("") }
    var condition_weeklyStats1 by remember { mutableStateOf("") }
    var condition_weeklyStats2 by remember { mutableStateOf("") }
    var condition_morningMarks by remember { mutableStateOf("") }
    var condition_count by remember { mutableStateOf("") }
    var condition_hour1 by remember { mutableStateOf("") }
    var condition_hour2 by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Başlık Girişi
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Başlık", color = MaterialTheme.colorScheme.onSecondary) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Açıklama Girişi
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Açıklama", color = MaterialTheme.colorScheme.onSecondary) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))


        Spacer(Modifier.height(12.dp))


        Divider()

        Text("\n\n\n\n\nKoşullar. En az bir koşul girilmesi yeterli\n\n\n\n", color = MaterialTheme.colorScheme.onSecondary)

        // Koşul Girişi
        TextField(
            value = condition_totalMarkedDays,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                condition_totalMarkedDays = filteredValue
            },
            label = { Text("Toplam işaretlenen gün koşulu (Örnek: Toplam 50 işaretleme)", color = MaterialTheme.colorScheme.onSecondary) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Koşul Girişi
        TextField(
            value = condition_maxStreak,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                condition_maxStreak = filteredValue
            },
            label = { Text("en uzun seri koşulu (Örnek: En çok 50 gün ard arda işaratleme)", color = MaterialTheme.colorScheme.onSecondary) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Koşul Girişi
        TextField(
            value = condition_currentStreak,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                condition_currentStreak = filteredValue
            },
            label = { Text("mevcut Seri koşulu (Örnek: Şu anki serin 30 gün)", color = MaterialTheme.colorScheme.onSecondary) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(24.dp))


        Text("Haftalık tamamlanma koşulu (Örnek: 10 farklı hafta 5+ gün işaretle.)\n", color = MaterialTheme.colorScheme.onSecondary)
        Row(Modifier.fillMaxWidth()){
            // Koşul Girişi
            TextField(
                value = condition_weeklyStats1,
                onValueChange = { newValue ->
                    val filteredValue = newValue.filter { it.isDigit() }
                    condition_weeklyStats1 = filteredValue
                },
                label = { Text("Kaç hafta boyunca (Örnek: 10 hafta boyunca)", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Koşul Girişi
            TextField(
                value = condition_weeklyStats2,
                onValueChange = { newValue ->

                    if(newValue==""){
                        condition_weeklyStats2  =newValue
                    }else{
                        val filteredValue =newValue.filter { it.isDigit() }
                        condition_weeklyStats2 = filteredValue.toIntOrNull()?.takeIf { it<8 }?.toString()?: "7"
                    }
                },
                label = { Text("Her hafta Kaç kere (Örnek: 7/7 işaretle.)", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Koşul Girişi
        TextField(
            value = condition_morningMarks,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                condition_morningMarks = filteredValue
            },
            label = { Text("sabah işaretleme adeti koşulu (Örnek: 3 sabah alışkanlık işaretle.)", color = MaterialTheme.colorScheme.onSecondary) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(24.dp))

        Text("Şu saatler arasında işaretleme koşulu (Örnek: 10 gün saat 10 ve saat 12 arası işaretle.)\n", color = MaterialTheme.colorScheme.onSecondary)
        Row(Modifier.fillMaxWidth()){
            // Koşul Girişi
            TextField(
                value = condition_count,
                onValueChange = { newValue ->
                    val filteredValue = newValue.filter { it.isDigit() }
                    condition_count = filteredValue
                },
                label = { Text("adet", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Koşul Girişi
            TextField(
                value = condition_hour1,
                onValueChange = { newValue ->
                    if(newValue==""){
                        condition_hour1  =newValue
                    }else{
                        val filteredValue =newValue.filter { it.isDigit() }
                        condition_hour1 = filteredValue.toIntOrNull()?.takeIf { it<24 }?.toString()?: "23"

                        condition_hour2.toIntOrNull()?.let{
                            condition_hour1.toIntOrNull()?.let{it1->
                                
                                if(it<it1){
                                    condition_hour2 = condition_hour1
                                }
                                
                                
                                
                                
                            }
                        }

                    }
                },
                label = { Text("Başlangıç saati", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Koşul Girişi
            TextField(
                value = condition_hour2,
                onValueChange = { newValue ->
                    if(newValue==""){
                        condition_hour2  =newValue
                    }else{
                        val filteredValue =newValue.filter { it.isDigit() }
                        condition_hour2 = filteredValue.toIntOrNull()?.takeIf { it<24 && (condition_hour1.toIntOrNull()?: 0)<=it }?.toString()?: ""
                    }

                },
                label = { Text("Bitiş saati", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }



        Spacer(modifier = Modifier.height(24.dp))

        // Başarı Ekleme Butonu
        Button(
            onClick = {
                // Burada koşulu işleyip başarıyı ekleme işlemi yapılır
                val newAchievement = AchievementUtils.Achievement(
                    name = name,
                    description = description,
                    condition = { userProgress ->
                        // Koşul işleme örneği
                        //condition == "Sabah 3 işaretleme" && userProgress.morningMarks >= 3
                        true
                    }
                )



                if(name.trim()!="" && description.trim()!=""){
                    achievementUtils.addCustomToSp(context.getSharedPreferences(context.packageName,MODE_PRIVATE)
                        ,name,description,condition_totalMarkedDays,
                        condition_maxStreak,condition_currentStreak,condition_weeklyStats1,
                        condition_weeklyStats2,condition_morningMarks,condition_count,condition_hour1,
                        condition_hour2
                    )
                    Toast.makeText(context, "Başarı Eklendi!", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Hata", Toast.LENGTH_SHORT).show()

                }


                // Toast mesajı gösterelim

                disMiss.invoke()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Başarı Ekle", color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}
