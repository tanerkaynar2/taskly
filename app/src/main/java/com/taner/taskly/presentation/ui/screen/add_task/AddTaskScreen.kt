package com.taner.taskly.presentation.ui.screen.add_task

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.ui.theme.TasklyTheme
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import com.taner.taskly.presentation.ui.components.DropdownSelector
import com.taner.taskly.presentation.ui.components.DatePickerField
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdfScanner
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.compose.currentBackStackEntryAsState
import com.taner.taskly.MainActivity.Companion.defaultCategoryData
import com.taner.taskly.MainActivity.Companion.defaultCategoryDetailData
import com.taner.taskly.MainActivity.Companion.defaultRepetitionData
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.core.utils.DateUtils.Companion
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.getWeeklySummaryText
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.core.utils.swap
import com.taner.taskly.presentation.ui.components.FilePickerSection
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.isInitHabitSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//add_task?taskId=${task.id}

@Composable
fun AddTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    editTaskId: Int?=null,
            dropdowns: String?=null,
    enableNotification: Boolean?=null
){

    
    LaunchedEffect(Unit) {

        if(!isInitHabitSection){
            isInitHabitSection = true}
    }
    val context = LocalContext.current

    // State'ler
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(dropdowns?.split(",")?.getOrNull(0)?.let{TaskCategory.entries.get(it.toInt())}?: defaultCategoryData?.let{

        fun getCat(str: String): TaskCategory?{
            return TaskCategory.entries.filter { it.turkishName==str }.firstOrNull()
        }

        if(it == "En Son"){
            defaultCategoryDetailData?.let{data->
                TaskCategory.entries.filter { it.turkishName == data }.firstOrNull().let{
                    it ?: TaskCategory.CUSTOM
                }
            }
        }else getCat(it)
    }?: TaskCategory.DAILY )}
    var customCategoryDetail by remember { mutableStateOf(dropdowns?.split(",")?.getOrNull(4).let{
        if(it!=null){
            it
        }else{
            if(category == TaskCategory.CUSTOM && defaultCategoryData == "En Son"){
                defaultCategoryDetailData.takeIf { it!="" }?: ""
            }else ""
        }
    }?: "") }
    var status by remember { mutableStateOf(dropdowns?.split(",")?.getOrNull(1)?.let{TaskStatus.entries.get(it.toInt())}?:TaskStatus.NOT_STARTED) }
    var priority by remember { mutableStateOf(dropdowns?.split(",")?.getOrNull(2)?.let{TaskPriority.entries.get(it.toInt())}?:TaskPriority.MEDIUM) }
    var notification by remember { mutableStateOf(false) }
    var repetition by remember { mutableStateOf(dropdowns?.split(",")?.getOrNull(3)?.let{
        TaskRepetition.entries.get(it.toInt())}.let{ if(it!=null) it else{ TaskRepetition.entries.get(defaultRepetitionData) } }) }
    var locations by remember { mutableStateOf(listOf<String>()) }
    var subTasks by remember { mutableStateOf(listOf<String>()) }
    var subTaskText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(colorList.random().hashCode()) }
    var attachments by remember { mutableStateOf(listOf<String>()) }
    var isCompleted by remember { mutableStateOf(false) }

    var showColorDialog by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }

    val dayOfWeek = ((android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
    val dayOfMonth = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_MONTH)   // 1-31

    var selectedDate = remember { mutableStateOf("") }
    var selectedTime = remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableStateOf(0L) }

    var selectedDaysForWeekly by remember { mutableStateOf(listOf<Int>()) }
    var selectedDaysForMonthly by remember { mutableStateOf(listOf<Int>()) }

    var simpleDialogStringData by remember { mutableStateOf<String?>(null) }
    var simpleDialogIsInputHide by remember { mutableStateOf(false) }
    var simpleDialogTitle by remember { mutableStateOf<String?>(null) }
    var simpleDialogAction by remember { mutableStateOf<Pair<String, (String?)->Unit>?>(null) }
    var simpleDialogAction2 by remember { mutableStateOf<Pair<String, (String?)->Unit>?>(null) }
    var simpleDialogResult by remember { mutableStateOf<(String?)->Unit>({}) }

    var showDialog by remember { mutableStateOf(false) }






    if(editTaskId!=null){
        LaunchedEffect(editTaskId) {
            editTaskId?.let{editTaskId->
                CoroutineScope(Dispatchers.IO).launch {

                    delay(200)

                    viewModel.getTaskById(editTaskId)?.let{
                        title = it.title
                        description = it.description?: ""
                        category = it.category
                        customCategoryDetail = it.customCategoryDetail?: ""
                        status = it.status
                        priority = TaskPriority.entries[it.priority]
                        notification = it.notification
                        repetition = it.repetition?: TaskRepetition.NONE
                        locations = it.locations?.split("||")?: emptyList()
                        subTasks = it.subTasks?.split("||")?: emptyList()
                        it.color?.let{color = it}
                        isCompleted = it.isCompleted
                        attachments = it.attachments?.split("||")?: emptyList()
                        viewModel.selectedFiles.value = emptyList()
                        it.attachments?.split("||")?.let{viewModel.addSelectedFiles(it.map { it.toUri() }) }

                        selectedTimestamp = it.date?: 0L
                        selectedTime.value = it.time?: ""
                        selectedDate.value = it.date?.let{dateFormat.format(it)}?: ""

                        it.days?.split("||")?.map { it.replace("->","") }?.let{
                            if(repetition == TaskRepetition.WEEKLY){
                                selectedDaysForWeekly = it.map { daysOfWeek.indexOf(it) }
                            }else if(repetition == TaskRepetition.MONTHLY){
                                selectedDaysForMonthly = it.map { it.toInt() }
                            }
                        }

                    }
                }
            }
        }
    }else{
        LaunchedEffect(Unit){
            viewModel.selectedFiles.value = emptyList()



        }
    }


    if(enableNotification==true){
        LaunchedEffect(Unit){
            delay(200)

            notification = true

        }
    }

    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(16.dp, 30.dp, 16.dp, 16.dp) ){

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Başlık", color = MaterialTheme.colorScheme.onSecondary) },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
            label = { Text("Açıklama", color = MaterialTheme.colorScheme.onSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp), colors = TextFieldDefaults.colors(cursorColor = if(isLightTheme()) Color.Black else Color.White,
                unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        , focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
            )
        )

        // Kategori Status, Priority, Repetition

        Spacer(modifier = Modifier.height(12.dp))

        "".let{Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            Spacer(modifier = Modifier.height(24.dp))

        } }

        val sp = remember { context.getSharedPreferences(context.packageName, MODE_PRIVATE) }




        val customLocations = remember { mutableStateOf<List<String>>(emptyList()) }
        val customCategories = remember { mutableStateOf<Set<String>?>(null) }

        LaunchedEffect(Unit) {
            customLocations.value = getLocations(sp)?.toList()?: emptyList()
            customCategories.value = TaskCategory.getCategories(sp)



        }


        Spacer(modifier = Modifier.height(8.dp))



        Column( modifier = Modifier
            .fillMaxWidth()/*
            .border(0.2.dp, Color.Gray)*/) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Kategori", "Görev Durumu", "Görev Önceliği","Tekrar Durumu").forEach { name ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.onSecondary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = name, color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(4){

                    when(it){
                        0->{
                            var items = TaskCategory.entries.map { it.turkishName }
                            customCategories?.value?.let{
                                it?.let { it1 -> items.toMutableList().apply{addAll(it1);items = this} }
                            }
                            Box(modifier = Modifier
                                .weight(1f)
                                //.border(1.dp, MaterialTheme.colorScheme.onSecondary)
                                .padding(0.dp, 8.dp, 0.dp, 2.dp),
                                contentAlignment = Alignment.Center) {


                                DropdownSelector(
                                    items = items,
                                    selectedItem = if(category==TaskCategory.CUSTOM) {customCategoryDetail.takeIf { it!="" }?: category} else category,
                                    onItemSelected = {sectedItem->
                                        category = when(sectedItem.toString()){
                                            TaskCategory.DAILY.turkishName->{
                                                TaskCategory.DAILY
                                            }
                                            TaskCategory.WEEKLY.turkishName->{
                                                TaskCategory.WEEKLY
                                            }
                                            else -> TaskCategory.CUSTOM
                                        }

                                        if(category == TaskCategory.CUSTOM){

                                            simpleDialogTitle = if(sectedItem.toString() == TaskCategory.CUSTOM.turkishName) "Yeni kategori Ekle" else "Seç"
                                            simpleDialogStringData = if(sectedItem.toString() == TaskCategory.CUSTOM.turkishName) customCategoryDetail else sectedItem.toString()
                                            customCategoryDetail = ""
                                            category = TaskCategory.DAILY
                                            simpleDialogResult = {
                                                if (it != null && it!="") {
                                                    customCategoryDetail = it
                                                    category = TaskCategory.CUSTOM

                                                    val newList = items+ customCategoryDetail
                                                    TaskCategory.saveCategories(sp,newList.filterNot { TaskCategory.entries.map { it.turkishName }.contains(it) })
                                                    items = newList
                                                }else{

                                                }
                                            }

                                            showDialog = true
                                        }
                                    }, onItemDelClicked = {
                                        val newList = items - it.toString()
                                        TaskCategory.saveCategories(sp,newList.filterNot { TaskCategory.entries.map { it.turkishName }.contains(it) })
                                        items = newList
                                    }, hideDelButtonIndexes = listOf(0,1,2)
                                )



                            }

                        }
                        1->{
                            Box(modifier = Modifier
                                .weight(1f)
                                //.border(1.dp, MaterialTheme.colorScheme.onSecondary)
                                .padding(0.dp, 8.dp, 0.dp, 2.dp),
                                contentAlignment = Alignment.Center) {
                                DropdownSelector(TaskStatus.entries.toList(), status) { status = it}
                            }
                        }
                        2->{

                            Box(modifier = Modifier
                                .weight(1f)
                                //  .border(1.dp, MaterialTheme.colorScheme.onSecondary)
                                .padding(0.dp, 8.dp, 0.dp, 2.dp),
                                contentAlignment = Alignment.Center) {
                                DropdownSelector(TaskPriority.values().toList(), priority) { priority = it }
                            }
                        }
                        3->{

                            Box(modifier = Modifier
                                .weight(1f)
                                // .border(1.dp, MaterialTheme.colorScheme.onSecondary)
                                .padding(0.dp, 8.dp, 0.dp, 2.dp),
                                contentAlignment = Alignment.Center) {
                                DropdownSelector(TaskRepetition.values().toList(), repetition) { repetition = it }
                            }

                        }
                    }

                }
            }




        }


        if (showDialog) {
            SimpleDialog(
                onDismiss = { showDialog = false

                    simpleDialogStringData = null
                    simpleDialogTitle = null
                    simpleDialogIsInputHide = false
                    simpleDialogAction = null
                    simpleDialogAction2 = null
                    simpleDialogResult = {}

                            },
                currentStringData = simpleDialogStringData,
                isInputHide = simpleDialogIsInputHide,
                action = simpleDialogAction,
                action2 = simpleDialogAction2,
                title = simpleDialogTitle,
                onConfirm = { result ->

                    simpleDialogResult(result)




                    showDialog = false

                }
            )
        }




        // Tarih formatlayıcı

        // Tarih seçici
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate.value = dateFormat.format(calendar.time)
                selectedTimestamp = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Saat seçici
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime.value = timeFormat.format(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        var showSelectDayDialog by remember { mutableStateOf(0) }


        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = when(repetition){
                TaskRepetition.DAILY->"Günlük Görev"
                TaskRepetition.MONTHLY->"Aylık Görev"
                TaskRepetition.WEEKLY->"Haftalık Görev"
                else->""
            },Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()){


                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Saat: ${selectedTime.value}",Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Saat Seç", color = MaterialTheme.colorScheme.onSecondary)
                    }

                }

                Spacer(modifier = Modifier.width(8.dp))


                if(repetition ==TaskRepetition.WEEKLY || repetition ==TaskRepetition.MONTHLY || repetition ==TaskRepetition.NONE){
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text =
                            (when(repetition){
                                TaskRepetition.MONTHLY ->{
                                    "Gün: ${ selectedDaysForMonthly.map { "${it + 1}" }.joinToString ( ", " )}"
                                }
                                TaskRepetition.WEEKLY->{
                                    "${getWeeklySummaryText(selectedDaysForWeekly)?: ""}"
                                }
                                else->"Tarih: ${selectedDate.value}"})
                            ,Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            when(repetition){
                                TaskRepetition.MONTHLY->showSelectDayDialog = 2
                                TaskRepetition.WEEKLY->showSelectDayDialog = 1
                                else->{
                                    showSelectDayDialog = 0
                                    datePickerDialog.show()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(when(repetition){
                                TaskRepetition.MONTHLY->"Gün"
                                TaskRepetition.WEEKLY->"Gün Seç"
                                else->"Tarih Seç"}, color = MaterialTheme.colorScheme.onSecondary)
                        }

                    }
                }

            }


            Spacer(modifier = Modifier.height(24.dp))

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
                                            if (selectedDaysForWeekly.contains(index)) selectedDaysForWeekly =
                                                selectedDaysForWeekly - index
                                            else selectedDaysForWeekly =
                                                selectedDaysForWeekly + index
                                        }
                                        .background(
                                            if (index == dayOfWeek - 1) {
                                                MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.05f else 0.4f)
                                            } else Color.Transparent
                                        )
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedDaysForWeekly.contains(index),
                                        onClick = { /*selectedDaysForWeekly = selectedDaysForWeekly + index*/ }
                                    )
                                    Text(text = day, color = if( index == dayOfWeek - 1) (if(isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary) else MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }else{

                            val days = List(31) { it + 1 } // 31 gün

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier
                            ) {
                                items(days.size) { index ->

                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable(onClick = {
                                                if (selectedDaysForMonthly.contains(index)) selectedDaysForMonthly =
                                                    selectedDaysForMonthly - index
                                                else selectedDaysForMonthly =
                                                    selectedDaysForMonthly + index
                                            })
                                            .aspectRatio(1f)
                                            .background(
                                                if (selectedDaysForMonthly.contains(index)) Color.Gray else Color.Transparent
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
                    TextButton(onClick = {
                        showSelectDayDialog=0
                    }) {
                        Text("Tamam", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }, containerColor =  if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray
            )
        }







       // Spacer(modifier = Modifier.height(48.dp))

        Divider(color = Color.Gray, thickness = 1.dp)


        Spacer(modifier = Modifier.height(24.dp))
        // Lokasyon
        "Lokasyon (isteğe bağlı)".let{Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), contentAlignment = Alignment.Center) { Text(text =it, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                TextField(
                    value = locationText,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
                    onValueChange = { locationText = it },
                    label = {

                       Row {
                           Text("Yer Ekle",color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.weight(1f))

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

                                   val list = mutableListOf("Ev", "İş", "Okul", "Spor Salonu", "Market", "Tatilde")

                                   customLocations?.value?.let {
                                       list.addAll(it)
                                   }
                                   list.forEachIndexed { index, item ->
                                       DropdownMenuItem(
                                           text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                                           onClick = {

                                               locations = locations + item

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
                    if (locationText.isNotBlank()) {
                        locations = locations + locationText
                        locationText = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))





            Spacer(modifier = Modifier.height(8.dp))



        } }


        Box(Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(8.dp)){
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items(locations.size) { index ->

                    if(index==0){
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clickable {
                                expanded = true
                            },contentAlignment = Alignment.Center
                        ){

                            Image(Icons.Default.ArrowDropDown,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.CenterEnd)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                locations.forEachIndexed { index, item ->
                                    DropdownMenuItem(
                                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                                        onClick = {


                                            simpleDialogTitle = "Düzenle"
                                            simpleDialogStringData = item
                                            simpleDialogAction = "KALDIR" to {
                                                locations = locations - item
                                            }
                                            simpleDialogResult = {
                                                it?.let{
                                                    locations = locations.toMutableList().apply{
                                                        this[index] = it
                                                    }
                                                }
                                            }
                                            showDialog = true


                                            expanded = false
                                        }
                                    )
                                }
                            }

                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .sizeIn(
                                maxWidth = 150.dp,
                                maxHeight = 100.dp,
                                minHeight = 40.dp,
                                minWidth = 50.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray)
                            .clickable {

                                val item = locations[index]


                                simpleDialogTitle = "Düzenle"
                                simpleDialogStringData = item
                                simpleDialogResult = {

                                    it?.let {

                                        locations = locations.toMutableList().apply {
                                            this[index] = it
                                        }
                                    }


                                }
                                showDialog = true

                                // Toast.makeText(context, "$item", Toast.LENGTH_SHORT).show()

                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val tex = locations[index]
                        Text(
                            text = tex,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier
                                .wrapContentWidth()
                                .padding(4.dp, 0.dp, 34.dp, 0.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Image(Icons.Default.Delete,null, colorFilter = ColorFilter.tint(Color.Red),

                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp)
                                .align(Alignment.CenterEnd)
                                .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                .clickable {

                                    val item = locations[index]
                                    locations = locations - item


                                }
                        )
                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        // Subtask ekleme
        "Alt Görevler".let{Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), contentAlignment = Alignment.Center) { Text(text =it, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                TextField(
                    value = subTaskText,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
                    onValueChange = { subTaskText = it },
                    label = { Text("Alt görev",color = MaterialTheme.colorScheme.onSecondary) },
                    modifier = Modifier.weight(1f), colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        , focusedContainerColor = ( (if(isLightTheme()) Color.White else Color.DarkGray))
                        ,
                        cursorColor = if(isLightTheme()) Color.Black else Color.White,
                        focusedIndicatorColor = ( (if(isLightTheme()) Color.Black else Color.White))
                    )
                )
                IconButton(onClick = {
                    if (subTaskText.isNotBlank()) {
                        subTasks = subTasks + subTaskText
                        subTaskText = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))





            Spacer(modifier = Modifier.height(8.dp))


        } }




        Box(Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(8.dp)){




            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)

                    ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {


                subTasks.forEachIndexed {  index, s ->
                    if(index==0){
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable {
                                expanded = true
                            },contentAlignment = Alignment.Center
                        ){

                            Row(modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically){
                                Image(Icons.Default.ArrowDropDown,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .width(40.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    subTasks.forEachIndexed { index, item ->
                                        DropdownMenuItem(
                                            text = { Text(item.split("<|>").first().toString(), color = MaterialTheme.colorScheme.onSecondary) },
                                            onClick = {

                                                val item = subTasks[index].split("<|>").first()
                                                val itemTick = subTasks[index].split("<|>").getOrNull(1) == "1"
                                                val itemDesc = subTasks[index].split("<|>").getOrNull(2)

                                                simpleDialogTitle = "Düzenle"
                                                simpleDialogStringData = item
                                                simpleDialogAction = "KALDIR" to {
                                                    subTasks = subTasks - subTasks[index]
                                                }
                                                simpleDialogAction2 = "ACIKLAMA" to {

                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        delay(100)

                                                        withContext(Dispatchers.Main){

                                                            simpleDialogTitle = "ACIKLAMAYI Düzenle"
                                                            simpleDialogStringData = itemDesc
                                                            simpleDialogAction = "KALDIR" to {
                                                                val n = subTasks.toMutableList()
                                                                n[index] = "$item<|>${if(itemTick) 1 else 0}<|>"
                                                                subTasks = n
                                                            }
                                                            simpleDialogResult = {
                                                                it?.let{
                                                                    val n = subTasks.toMutableList()
                                                                    n[index] = "$item<|>${if(itemTick) 1 else 0}<|>$it"
                                                                    subTasks = n
                                                                }
                                                            }
                                                            showDialog = true
                                                        }
                                                    }
                                                }
                                                simpleDialogResult = {
                                                    it?.let{
                                                        val list = subTasks.toMutableList()
                                                        list[index] = if(itemDesc!=null){
                                                            it + "<|>" + (if(itemTick) 1 else 0) + "<|>" + itemDesc
                                                        }else it
                                                        subTasks = list
                                                    }
                                                }
                                                showDialog = true


                                                expanded = false
                                            }
                                        )
                                    }
                                }

                                Text(text = "   ${subTasks.size} Alt Görev",color = MaterialTheme.colorScheme.onSecondary)
                            }

                        }

                        Spacer(modifier = Modifier.height(24.dp))

                    }

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .sizeIn(maxHeight = 140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray)
                            .clickable {

                                val item = subTasks[index].split("<|>").first()
                                val itemTick = subTasks[index].split("<|>").getOrNull(1) == "1"
                                val itemDesc = subTasks[index].split("<|>").getOrNull(2)



                                simpleDialogTitle = "Düzenle"
                                simpleDialogStringData = item
                                simpleDialogResult = {

                                    it?.let {

                                        subTasks = subTasks.toMutableList().apply {
                                            this[index] = if (itemDesc != null) {
                                                it + "<|>" + (if (itemTick) 1 else 0) + "<|>" + itemDesc
                                            } else it
                                        }
                                    }


                                }
                                showDialog = true

                                // Toast.makeText(context, "$item", Toast.LENGTH_SHORT).show()

                            },
                        contentAlignment = Alignment.Center
                    ) {


                        val tex = subTasks[index].split("<|>").first()
                        val itemTick = subTasks[index].split("<|>").getOrNull(1) == "1"
                        val itemDesc = subTasks[index].split("<|>").getOrNull(2)?.takeIf { it!="null" }


                        Row ( Modifier
                            .fillMaxWidth()
                            .padding(16.dp) ){

                            Checkbox(modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),checked = itemTick,onCheckedChange = {
                                val item = subTasks[index]

                                val tex = item.split("<|>").first()
                                val itemTick = item.split("<|>").getOrNull(1) == "1"
                                val itemDesc = item.split("<|>").getOrNull(2)

                                subTasks = subTasks.toMutableList().apply { this[index] = "$tex<|>${if(it) 1 else 0}<|>$itemDesc" }.toList()

                            })


                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "    •   ${tex}",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier
                                    .weight(1f)// .fillMaxWidth()
                                    .padding(4.dp, 0.dp, 34.dp, 0.dp)
                            )


                            if(index!=0){
                                Image(Icons.Default.MoveUp,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),

                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        // .align(Alignment.CenterEnd)
                                        .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                        .clickable {

                                            val newIndex = index - 1
                                            val list = subTasks.toMutableList()
                                            list[newIndex] = subTasks[index]
                                            list[index] = subTasks[newIndex]
                                            subTasks = list

                                        }
                                )
                            }


                            if(index!=subTasks.lastIndex){
                                Image(Icons.Default.MoveDown,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),

                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        // .align(Alignment.CenterEnd)
                                        .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                        .clickable {

                                            val newIndex = index + 1
                                            val list = subTasks.toMutableList()
                                            list[newIndex] = subTasks[index]
                                            list[index] = subTasks[newIndex]
                                            subTasks = list

                                        }
                                )
                            }


                            Image(Icons.Default.EditNote,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),

                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                                    // .align(Alignment.CenterEnd)
                                    .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                    .alpha(if (itemDesc != null && itemDesc != "") 1f else 0.2f)
                                    .clickable {

                                        simpleDialogTitle = "Açıklama Ekle"
                                        simpleDialogStringData = itemDesc
                                        simpleDialogResult = {
                                            if (it != null) {
                                                val n = subTasks.toMutableList()
                                                n[index] = "$tex<|>${if (itemTick) 1 else 0}<|>$it"
                                                subTasks = n
                                            }
                                        }

                                        showDialog = true

                                    }
                            )



                            Image(Icons.Default.Delete,null, colorFilter = ColorFilter.tint(Color.Red),

                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                                    // .align(Alignment.CenterEnd)
                                    .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                    .clickable {

                                        val item = subTasks[index]
                                        subTasks = subTasks - item

                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                }

            }
        }



        Spacer(modifier = Modifier.height(8.dp))

        if (showColorDialog) {
            FullColorPickerDialog(
                onDismiss = { showColorDialog = false },
                onColorSelected = {
                    color = it.hashCode()
                    showColorDialog = false
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Divider(color = Color.Gray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Renk seçimi (basit)


        Row(modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { showColorDialog = true },verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(color))
            )
            Text("       Renk Seç",Modifier
                .weight(1f)
                ,MaterialTheme.colorScheme.onSecondary)
        }



        Spacer(modifier = Modifier.height(8.dp))
        // Tamamlandı
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
            Text("Tamamlandı",Modifier.fillMaxWidth(),MaterialTheme.colorScheme.onSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        var notificationIntentOnce = remember { mutableStateOf(true) }
        var showEnableNotDialog = remember { mutableStateOf(false) }

        // Bildirim
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = notification, onCheckedChange = {

                fun goOn(){

                    if(it){
                        sp.getInt("reminderOffsetMinutes",-1).takeIf { it>-1 }.let{v->
                            if(v==null){

                                showEnableNotDialog.value = true

                                notification = false
                            }else{
                                notification = it
                            }
                        }
                    }else {
                        notification = it
                    }


                }

                if(it){



                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1 // İzin isteği kodu
                        )
                    }else goOn()


                }else{
                    goOn()
                }


            })
            Text("Bildirim Açık",Modifier.fillMaxWidth(),MaterialTheme.colorScheme.onSecondary)
        }
        Spacer(modifier = Modifier.height(24.dp))

        if(showEnableNotDialog.value){
            AlertDialog(
                onDismissRequest = {showEnableNotDialog.value=false},
                title = {},
                text = {
                    Text(
                        "Bildirim ayarlarınız kapalı. Lütfen hatırlatıcı ayarlarını kontrol edin.",
                        color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSecondary)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            NotificationUtils.enableNotification(sp)
                            showEnableNotDialog.value=false
                        }
                    ) {
                        Text("Aç", color = MaterialTheme.colorScheme.onSecondary)
                    }
                },
                containerColor = if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else  Color.DarkGray
            )
        }

        Divider(color = Color.Gray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(32.dp))

        // Attachments
        Column (horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Dosya Ekle",Modifier.wrapContentWidth(),MaterialTheme.colorScheme.onSecondary)

            Spacer(modifier = Modifier.height(8.dp))

            FilePickerSection(
                selectedFiles = viewModel.selectedFiles.value,
                onFilesSelected = {
                    viewModel.addSelectedFiles(it);/*.setSelectedFiles(it)*/
                    attachments = viewModel.selectedFiles.value.map { it.toString() }
                    viewModel.selectedFiles.value.forEach {
                        val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        context.contentResolver.takePersistableUriPermission(it, takeFlags)
                    }
                                  },
                onFilesRemove = {
                    viewModel.removeSelectedFiles(it)
                    attachments = viewModel.selectedFiles.value.map { it.toString() }

                },

            )

            Spacer(modifier = Modifier.height(16.dp))

        }




        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {

                    if(selectedTime.value==""){
                        timePickerDialog.show()
                    }
                    else if(repetition == TaskRepetition.WEEKLY && selectedDaysForWeekly.isEmpty()){
                        showSelectDayDialog = 1
                    }else if(repetition == TaskRepetition.MONTHLY && selectedDaysForMonthly.isEmpty()){
                        showSelectDayDialog = 2
                    }else if(repetition == TaskRepetition.NONE && (selectedTimestamp<=0L)){
                        showSelectDayDialog = 0
                        if(selectedTimestamp<=0L ){
                            datePickerDialog.show()
                        }
                    }else{




                        if(editTaskId==null){




                            val newLocations = locations.filterNot { customLocations?.value?.let{it1-> it in it1}?: false }
                            if (newLocations.isNotEmpty()) {
                                if(customLocations==null) customLocations.value = emptyList()
                                val bef = customLocations?.value?.toMutableList()
                                bef?.addAll(newLocations)
                                bef?.let{customLocations.value = it}
                                customLocations?.value?.toList()?.let { saveLocations(sp, it) }
                            }


                            val newTask = Task(
                                title = title,
                                description = description.ifBlank { null },
                                date = selectedTimestamp.takeIf { it>0 },
                                days = when(repetition){
                                    TaskRepetition.WEEKLY->{
                                        selectedDaysForWeekly.takeIf { it.isNotEmpty() }?.map { daysOfWeek.get(it) }?.sorted()?.joinToString("||")
                                    }
                                    TaskRepetition.MONTHLY->{
                                        selectedDaysForMonthly.takeIf { it.isNotEmpty() }?.sorted()?.joinToString("||"){"->$it"}
                                    }
                                    else->null
                                },
                                category = category,
                                customCategoryDetail = customCategoryDetail,
                                time = selectedTime.value.takeIf { it!="" },
                                status = if(isCompleted) TaskStatus.COMPLETED else status,
                                priority = priority,
                                notification = notification,
                                repetition = repetition,
                                locations = locations.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                subTasks = subTasks.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                color = color,
                                attachments = attachments.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                isCompleted = status == TaskStatus.COMPLETED || isCompleted,
                                lastCompletedDate = (dateFormat3.format(Date())).takeIf { status == TaskStatus.COMPLETED || isCompleted }
                            )
                            val newTaskEntity = newTask.toEntity()
                            viewModel.addTask(newTaskEntity)
                            navController.navigate("home") {
                                popUpTo("add_task") { inclusive = true }
                                launchSingleTop = true
                            }


                            if(defaultCategoryData == "En Son"){
                                defaultCategoryDetailData = category.let{
                                    if(it == TaskCategory.CUSTOM){
                                        customCategoryDetail.takeIf { it!="" }?: TaskCategory.DAILY.turkishName
                                    }else it.turkishName
                                }
                            }


                            if(notification){

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




                            }

                        }else {

                            val newLocations = locations.filterNot { customLocations?.value?.let{it1-> it in it1}?: false }
                            if (newLocations.isNotEmpty()) {
                                if(customLocations==null) customLocations.value = emptyList()
                                val bef = customLocations?.value?.toMutableList()
                                bef?.addAll(newLocations)
                                bef?.let{customLocations.value = it}
                                customLocations?.value?.toList()?.let { saveLocations(sp, it) }
                            }


                            val newTask = Task(
                                id = editTaskId,
                                title = title,
                                description = description.ifBlank { null },
                                date = selectedTimestamp.takeIf { it>0 },
                                days = when(repetition){
                                    TaskRepetition.WEEKLY->{
                                        selectedDaysForWeekly.takeIf { it.isNotEmpty() }?.map { daysOfWeek.get(it) }?.sorted()?.joinToString("||")
                                    }
                                    TaskRepetition.MONTHLY->{
                                        selectedDaysForMonthly.takeIf { it.isNotEmpty() }?.sorted()?.joinToString("||"){"->$it"}
                                    }
                                    else->null
                                },
                                category = category,
                                customCategoryDetail = customCategoryDetail,
                                time = selectedTime.value.takeIf { it!="" },
                                status = if(isCompleted) TaskStatus.COMPLETED else status,
                                priority = priority,
                                notification = notification,
                                repetition = repetition,
                                locations = locations.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                subTasks = subTasks.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                color = color,
                                attachments = attachments.takeIf { it.isNotEmpty() }?.joinToString("||"),
                                isCompleted = status == TaskStatus.COMPLETED || isCompleted,
                                lastCompletedDate = (dateFormat3.format(Date())).takeIf { status == TaskStatus.COMPLETED || isCompleted }
                            )
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
                            }
                            val newTaskEntity = newTask.toEntity()



                            viewModel.updateTask(newTaskEntity)

                            if(!notification || newTask.isCompleted){
                                val notificationManager =
                                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancel(editTaskId)
                                NotificationUtils.cancelScheduledNotification(context,editTaskId)

                                notViewModel?.deleteNotificationsByTaskId(newTask.id)


                            }else{
                                notViewModel?.deleteNotificationsByTaskId(newTask.id)

                                if(notification && !newTask.isCompleted) NotificationUtils.addNotificationToDatabase(context,newTask)


                            }



                            navController.popBackStack()/*.navigate("home") {
                            popUpTo("add_task") { inclusive = true }
                            launchSingleTop = true
                        }*/
                        }




                    }


                } else {
                    Toast.makeText(context, "Başlık boş olamaz", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if(editTaskId==null) "Görev Ekle" else "Güncelle",color= MaterialTheme.colorScheme.onSecondary)
        }
    }
}


@Composable
fun SimpleDialog(
    currentStringData: String?=null,
    isInputHide: Boolean=false,
    action: (Pair<String, (String?)->Unit>)?=null,
    action2: (Pair<String, (String?)->Unit>)?=null,
    title: String?=null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("${currentStringData?: ""}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title?: "Özel: ", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary)
        },
        text = {
            Column {
                Row(Modifier.fillMaxWidth().background(Color.Transparent)) {
                    if(!isInputHide){
                        TextField(
                            value = text,
                            onValueChange = { input ->
                                text = input

                            },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.fillMaxWidth(),
                            //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                            colors = TextFieldDefaults.colors( focusedContainerColor = if(isLightTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
                                cursorColor = if(isLightTheme()) Color.Black else Color.White,
                                unfocusedIndicatorColor = Color.Gray, focusedTextColor = MaterialTheme.colorScheme.primary)

                        )
                    }


                }
            }
        },
        confirmButton = {


            if(action2!=null){
                TextButton(
                    onClick = { action2.second(text);onDismiss() },
                    enabled = text.isNotEmpty()
                ) {
                    Text(action2.first, color = MaterialTheme.colorScheme.onSecondary)
                }

            }


            if(action!=null){
                TextButton(
                    onClick = { action.second(text);onDismiss() },
                    enabled = text.isNotEmpty()
                ) {
                    Text(action.first, color = MaterialTheme.colorScheme.onSecondary)
                }
            }
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotEmpty()

            ) {
                Text("TAMAM", color = MaterialTheme.colorScheme.onSecondary)
            }

        },
        containerColor = if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else  Color.DarkGray
        , modifier = Modifier.sizeIn(maxHeight = 500.dp)
    )
}




fun saveLocations(sp: SharedPreferences, list: List<String>){
    sp.edit().putStringSet("LOCATION",list.toSet()).apply()
}
fun getLocations(sp: SharedPreferences): MutableSet<String>? {
    return sp.getStringSet("LOCATION",  null)
}

// Renk paleti (Material renkler + pastel tonlar + ek tonlar)
val colorList = listOf(
    Color.Red, Color.Green, Color.Blue, Color.Yellow,
    Color.Cyan, Color.Magenta, Color.Gray, Color.Black, Color.White,

    // Material Colors
    Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFF018786),
    Color(0xFFB00020), Color(0xFFFF5722), Color(0xFFFFC107),
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),

    // Pastel Colors
    Color(0xFFFFCDD2), Color(0xFFE1BEE7), Color(0xFFBBDEFB),
    Color(0xFFB2DFDB), Color(0xFFC8E6C9), Color(0xFFFFF9C4),
    Color(0xFFFFE0B2), Color(0xFFD7CCC8), Color(0xFFCFD8DC),

    // Ekstra renk tonları
    Color(0xFF3E2723), Color(0xFF1B5E20), Color(0xFF0D47A1),
    Color(0xFF263238), Color(0xFF880E4F), Color(0xFF4A148C),
    Color(0xFF01579B), Color(0xFF1A237E), Color(0xFF004D40),
)
@Composable
fun FullColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Renk Seç", color = if(isLightTheme()) MaterialTheme.colorScheme.background else  MaterialTheme.colorScheme.onSecondary)
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                items(colorList.size) { index ->
                    val color = colorList[index]
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                            .clickable {
                                onColorSelected(color)
                                onDismiss()
                            }
                    )
                }
            }
        },
        confirmButton = {},
        containerColor =  if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else  Color(0xFF2B2B2B)
    )
}



/*@Composable
fun CustomTopAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = "Görev Ekle",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
fun AddTaskForm(viewModel: TaskViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CustomTopAppBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        val newTask = TaskEntity(
                            id = 0,
                            title = title,
                            description = description,
                            date = System.currentTimeMillis(),
                            isHabit = false,
                            isCompleted = false
                        )
                        viewModel.addTask(newTask)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ekle")
            }
        }
    }
}*/