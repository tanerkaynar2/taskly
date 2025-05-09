package com.taner.taskly.presentation.ui.screen.habits


import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.taner.taskly.MainActivity.Companion.defaultCategoryData
import com.taner.taskly.MainActivity.Companion.defaultCategoryDetailData
import com.taner.taskly.MainActivity.Companion.defaultScreenLayout
import com.taner.taskly.MainActivity.Companion.homeScreenLayout
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.getTimeOnlyTimestamp
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.ui.screen.add_task.FullColorPickerDialog
import com.taner.taskly.presentation.ui.screen.add_task.colorList
import com.taner.taskly.presentation.ui.screen.add_task.getLocations
import com.taner.taskly.presentation.ui.screen.add_task.saveLocations
import com.taner.taskly.presentation.ui.screen.settings.SettingItem
import com.taner.taskly.presentation.ui.theme.TasklyTheme
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun AddHabitScreen(
    navController: NavController,
    viewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    editHabitId: Int?=null,
){

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var explain by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(listOf<Int>()) }
    var isActive by remember { mutableStateOf(true) }
    var isImportant by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var reminderTimeStamp by remember { mutableStateOf<Long?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<String?>(null) }
    var color by remember { mutableStateOf<Int?>(colorList.random().hashCode()) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var longestStreakYear by remember { mutableStateOf<Int?>(null) }
    var longestStreakDayOfYear by remember { mutableStateOf<Int?>(null) }
    var catText by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }

    val dayOfWeek = ((android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
    val dayOfMonth = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_MONTH)   // 1-31

    val sp = remember { context.getSharedPreferences(context.packageName, MODE_PRIVATE) }
    val customCats = remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        customCats.value = getCategories(sp)?.toList()?: emptyList()
    }

    if(editHabitId!=null){
        LaunchedEffect(Unit) {
            editHabitId?.let{editHabitId->
                CoroutineScope(Dispatchers.IO).launch {

                    delay(200)
                    viewModel.getHabitById(editHabitId)?.let{
                        it.toDomain().let {
                            name = it.name
                            explain = it.explain
                            days = it.days
                            isActive = it.isActive
                            isImportant = it.isImportant
                            frequency = it.frequency
                            createdAt = it.createdAt
                            reminderTimeStamp = it.reminderTimeStamp
                            category = it.category
                            note = it.note
                            color = it.color
                            currentStreak = it.currentStreak
                            longestStreak = it.longestStreak
                            longestStreakYear = it.longestStreakYear
                            longestStreakDayOfYear = it.longestStreakDayOfYear

                            catText = category?:""

                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("İsim", color = MaterialTheme.colorScheme.onSecondary) },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = explain,
            onValueChange = { explain = it },
            label = { Text("Açıklama", color = MaterialTheme.colorScheme.onSecondary) },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = note?: "",
            onValueChange = { note = it.takeIf { it!="" } },
            label = { Text("Not Ekle", color = MaterialTheme.colorScheme.onSecondary) },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        var expanded by remember { mutableStateOf(false) }
        var showConfirmation by remember { mutableStateOf(false) }
        var newFrequency by remember { mutableStateOf(frequency) }

        fun confirmFrequency(){
            if(frequency != newFrequency){
                days = emptyList()
                frequency = newFrequency
                expanded = false
            }
        }

        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmation = false
                    expanded = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        currentStreak = 0

                        confirmFrequency()


                        showConfirmation = false
                    }) {
                        Text("Onayla", color = MaterialTheme.colorScheme.onSecondary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmation = false
                        expanded = false
                    }) {
                        Text("Vazgeç", color = MaterialTheme.colorScheme.onSecondary)
                    }
                },
                title = { Text("Uyarı", color = MaterialTheme.colorScheme.onSecondary) },
                text = { Text("Tekrar türünü değiştirirsen mevcut serin sıfırlanır. ", color = MaterialTheme.colorScheme.onSecondary) },
                containerColor = if (isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray
            )
        }


        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.align(Alignment.End),
            onDismissRequest = { expanded = false }
        ) {

            val list = HabitFrequency.entries.map { it.turkishName }

            list.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                    onClick = {
                        newFrequency = HabitFrequency.entries.get(index)
                        if(frequency != newFrequency){
                            if(editHabitId==null){
                                confirmFrequency()
                            }else{
                                showConfirmation = true
                            }
                        }
                    }
                )
            }
        }

        SettingItem(title = "Tekrar Türü", value = frequency.turkishName){

            expanded = true

        }

        Spacer(modifier = Modifier.height(24.dp))


        var showSelectDayDialog by remember { mutableStateOf(0) }


        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = when(frequency){
                HabitFrequency.DAILY->"Günlük Alışkanlık"
                HabitFrequency.MONTHLY->"Aylık Alışkanlık"
                HabitFrequency.WEEKLY->"Haftalık Alışkanlık"
                else->""
            },Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()){

                if(frequency!=HabitFrequency.DAILY){
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text =
                            (when(frequency){
                                HabitFrequency.MONTHLY ->{
                                    "Gün: ${ days.map { "${it}" }.joinToString ( ", " )}"
                                }
                                HabitFrequency.WEEKLY->{
                                    "${getWeeklySummaryText(days.map { it-1 }) ?: ""}"
                                }
                                else->""})
                            ,Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            when(frequency){
                                HabitFrequency.MONTHLY->showSelectDayDialog = 2
                                HabitFrequency.WEEKLY->showSelectDayDialog = 1
                                else->{
                                    showSelectDayDialog = 0
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(when(frequency){
                                HabitFrequency.MONTHLY->"Gün"
                                HabitFrequency.WEEKLY->"Gün Seç"
                                else->"Tarih Seç"}, color = MaterialTheme.colorScheme.onSecondary)
                        }

                    }
                }

            }


            Spacer(modifier = Modifier.height(24.dp))

        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isActive, onCheckedChange = {isActive = it})
            Text("Aktifleştir",Modifier.fillMaxWidth(),MaterialTheme.colorScheme.onSecondary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isImportant, onCheckedChange = {isImportant = it})
            Text("Önemli",Modifier.fillMaxWidth(),MaterialTheme.colorScheme.onSecondary)
        }



        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                reminderTimeStamp = getTimeOnlyTimestamp(hourOfDay,minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = reminderTimeStamp!=null, onCheckedChange = {
                if(it){
                    timePickerDialog.show()
                }else{
                    reminderTimeStamp = null
                }
            })
            Text(if(reminderTimeStamp==null) "Hatırlatıcı Oluştur"
                else "Hatırlatıcı zamanı:${DateUtils.getHourAndMinuteFromTimestamp(reminderTimeStamp!!).toList().joinToString(":")}",
                Modifier.fillMaxWidth(),MaterialTheme.colorScheme.onSecondary)
        }



        Spacer(modifier = Modifier.height(24.dp))


        "Kategori (isteğe bağlı)".let{Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), contentAlignment = Alignment.Center) {
            Text(text =it, color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                TextField(
                    value = catText,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
                    onValueChange = { catText = it ; category = catText},
                    label = {

                        Row {
                            Text("Kategori Ekle",color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.weight(1f))

                            var expanded by remember { mutableStateOf(false) }

                            Box(modifier = Modifier
                                .width(24.dp)
                                .height(24.dp)
                                .clickable {
                                    expanded = true
                                },contentAlignment = Alignment.Center
                            ){

                                Image(Icons.Default.AddLocationAlt,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.CenterEnd)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {

                                    val list = mutableSetOf(
                                        "Çalışma / İş",
                                        "Ev / Günlük İşler",
                                        "Sağlık / Fitness",
                                        "Sosyal",
                                        "Kişisel Gelişim",
                                        "Finans / Para Yönetimi",
                                        "Hobi / Eğlence",
                                        "Evcil Hayvanlar",
                                        "Alışveriş",
                                        "Yaratıcılık",
                                        "Gönüllülük / Yardım",
                                        "Teknoloji"
                                    )

                                    customCats?.value?.let {
                                        list.addAll(it)
                                    }

                                    list.forEachIndexed { index, item ->
                                        DropdownMenuItem(
                                            text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                                            onClick = {

                                                category = item
                                                catText = category?: ""

                                                expanded = false
                                            }
                                        )
                                    }
                                }

                            }
                        }

                    },
                    modifier = Modifier.weight(1f), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                        unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
                    )
                )
                IconButton(onClick = {
                    if (catText.isNotBlank()) {
                        category = null
                        catText = ""
                    }
                }) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))





            Spacer(modifier = Modifier.height(8.dp))



        } }

        Spacer(modifier = Modifier.height(24.dp))

        var showColorDialog by remember { mutableStateOf(false) }
        if (showColorDialog) {
            FullColorPickerDialog(
                onDismiss = { showColorDialog = false },
                onColorSelected = {
                    color = it.hashCode()
                    showColorDialog = false
                }
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { showColorDialog = true },verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color?.let { Color(it) }?: Color.Transparent)
            )
            Text("       Renk Seç",Modifier
                .weight(1f)
                ,MaterialTheme.colorScheme.onSecondary)
        }



        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {

                    if(frequency == HabitFrequency.DAILY){
                        days = (1..7).toList()
                    }
                    if(frequency == HabitFrequency.WEEKLY && days.isEmpty()){
                        showSelectDayDialog = 1
                    }else if(frequency == HabitFrequency.MONTHLY && days.isEmpty()){
                        showSelectDayDialog = 2
                    }else{




                        if(editHabitId==null){




                            category.takeIf { it?.isNotBlank()==true }?.let { cat ->
                                if (customCats.value.none { it.equals(cat.trim(), ignoreCase = true) }) {
                                    val updated = customCats.value.toMutableList()
                                    updated.add(cat.trim())
                                    customCats.value = updated
                                    saveCategories(sp, updated)
                                }
                            }



                            val newHabit = Habit(
                                name = name,
                                explain = explain,
                                days = days.sorted(),
                                isActive = isActive,
                                isImportant = isImportant,
                                frequency = frequency,
                                createdAt = createdAt,
                                reminderTimeStamp = reminderTimeStamp?.takeIf { it>0L },
                                category = category.takeIf { it!="" }?: catText,
                                note = note?.takeIf { it!="" },
                                color = color,)
                            val newHabitEntity = newHabit.toEntity()
                            viewModel.addHabit(newHabitEntity)
                            navController.navigate("habits") {
                                popUpTo("add_habit") { inclusive = true }
                                launchSingleTop = true
                            }


                            /*if(notification){

                                sp.getInt("reminderOffsetMinutes",-1).takeIf { it>-1 }.let{

                                    if(it==null){
                                    }else{
                                        CoroutineScope(Dispatchers.IO).launch {
                                            (viewModel.getLastTaskId()?: 1).let{id->
                                                NotificationUtils.addNotificationToDatabase(context,newTask.copy(id=id))
                                            }
                                        }
                                    }

                                }

                            }*/

                        }else {


                            category.takeIf { it?.isNotBlank()==true }?.let { cat ->
                                if (customCats.value.none { it.equals(cat.trim(), ignoreCase = true) }) {
                                    val updated = customCats.value.toMutableList()
                                    updated.add(cat.trim())
                                    customCats.value = updated
                                    saveCategories(sp, updated)
                                }
                            }

                            val newHabit = Habit(
                                id = editHabitId,
                                name = name,
                                explain = explain,
                                days = days.sorted(),
                                isActive = isActive,
                                isImportant = isImportant,
                                frequency = frequency,
                                createdAt = createdAt,
                                reminderTimeStamp = reminderTimeStamp?.takeIf { it>0L },
                                category = category.takeIf { it!="" }?: catText,
                                note = note?.takeIf { it!="" },
                                currentStreak = currentStreak,
                                longestStreak = longestStreak,
                                longestStreakDayOfYear = longestStreakDayOfYear,
                                longestStreakYear = longestStreakYear,
                                color = color,)

                            if(newHabit.reminderTimeStamp==null || !newHabit.isActive){
                                NotificationUtils.cancelScheduledNotification(context, habitId = newHabit.id)
                            }

                            /*
                            if(notification) {
                                newTask.notificationDelayMin = null
                                newTask.lastNotificationTime = newTask.date?.let{date->Calendar.getInstance().apply {
                                    timeInMillis = date

                                    // Saat yoksa günün başı
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)

                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis}
                            }*/
                            val newHabitEntity = newHabit.toEntity()



                            viewModel.updateHabit(newHabitEntity)

                         /*   if(!notification || newTask.isCompleted){
                                val notificationManager =
                                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancel(editTaskId)
                                NotificationUtils.cancelScheduledNotification(context,editTaskId)

                                notViewModel?.deleteNotificationsByTaskId(newTask.id)


                            }else{
                                notViewModel?.deleteNotificationsByTaskId(newTask.id)

                                if(notification && !newTask.isCompleted) NotificationUtils.addNotificationToDatabase(context,newTask)


                            }*/



                            navController.popBackStack()/*.navigate("home") {
                            popUpTo("add_task") { inclusive = true }
                            launchSingleTop = true
                        }*/
                        }




                    }


                } else {
                    Toast.makeText(context, "İsim boş olamaz", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if(editHabitId==null) "Ekle" else "Güncelle",color= MaterialTheme.colorScheme.onSecondary)
        }







        if(showSelectDayDialog!=0){
            AlertDialog(
                onDismissRequest = {showSelectDayDialog=0},
                title = { Text("Gün Seç", color = if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onSecondary) },
                text = {
                    Column {
                        if(showSelectDayDialog == 1){
                            daysOfWeek.forEachIndexed { index, day ->

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (days.contains(index+1)) days =
                                                days - (index+1)
                                            else days = days + (index + 1)
                                        }
                                        .background(
                                            if (index == dayOfWeek - 1) {
                                                MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.05f else 0.4f)
                                            } else Color.Transparent
                                        )
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = days.contains(index+1),
                                        onClick = { /*selectedDaysForWeekly = selectedDaysForWeekly + index*/ }
                                    )
                                    Text(text = day, color = if( index == dayOfWeek - 1) (if(isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary) else MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }else{

                            val dayys = List(31) { it + 1 } // 31 gün

                            LazyVerticalGrid (
                                columns = GridCells.Fixed(7),
                                modifier = Modifier
                            ) {
                                items(dayys.size) { index ->

                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable(onClick = {
                                                if (days.contains(index + 1)) days =
                                                    days - (index + 1)
                                                else days =
                                                    days + (index + 1)
                                            })
                                            .aspectRatio(1f)
                                            .background(
                                                if (days.contains(index+1)) Color.Gray else Color.Transparent
                                            )
                                            .border(
                                                if (index == dayOfMonth-1) {
                                                    1.dp
                                                } else 0.dp, if (index == dayOfMonth-1) {
                                                    MaterialTheme.colorScheme.onSecondary
                                                } else Color.Transparent
                                            )
                                            .padding(10.dp)
                                            .align(Alignment.CenterHorizontally)

                                            .fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index+1}",
                                            fontSize = 12.sp,
                                            style = TextStyle(
                                                color  = MaterialTheme.colorScheme.onSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                }
                            }



                        }

                    }
                },
                confirmButton = {
                    TextButton (onClick = {
                        showSelectDayDialog=0
                    }) {
                        Text("Tamam", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }, containerColor =  if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray
            )
        }



    }



}

fun getCategories(sp: SharedPreferences): MutableSet<String>? {
    return sp.getStringSet("HABIT_CATEGORIES",  null)
}

fun saveCategories(sp: SharedPreferences, list: List<String>){
    sp.edit().putStringSet("HABIT_CATEGORIES",list.toSet()).apply()
}
/*@Composable
fun HabitScreenContent(viewModel: HabitViewModel = viewModel()) {
    var habitTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alışkanlıklar") },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = habitTitle,
                onValueChange = { habitTitle = it },
                label = { Text("Alışkanlık Başlığı") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (habitTitle.isNotEmpty()) {
                        // Add habit to ViewModel
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ekle")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Display list of habits here
            // LazyColumn for habits
        }
    }
}
*/