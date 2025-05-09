package com.taner.taskly.presentation.ui.screen.home


import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.taner.taskly.presentation.ui.components.HabitSection
import com.taner.taskly.presentation.ui.components.StatsCard
import com.taner.taskly.presentation.ui.components.TaskCard
import com.taner.taskly.presentation.ui.components.WeekCalendar
import java.util.Calendar
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.taner.taskly.MainActivity.Companion.defaultTaskOrder
import com.taner.taskly.MainActivity.Companion.defaultTimeScope
import com.taner.taskly.MainActivity.Companion.homeScreenLayout
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isEnabledAnimation
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.MainActivity.Companion.isSyncEnabled
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.MainActivity.Companion.saveLastSYNC
import com.taner.taskly.core.utils.DateUtils
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat3
import com.taner.taskly.core.utils.DateUtils.Companion.getDayOfYearAndDaYear
import com.taner.taskly.core.utils.DateUtils.Companion.getFormattedDate
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.HabitCheck
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskPriority
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import com.taner.taskly.presentation.ui.screen.add_task.getLocations
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.habitChecks
import kotlinx.coroutines.withContext
import java.util.Date

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    habitViewModel: HabitViewModel
){
    val scrollState = rememberScrollState()
    val scrollState2 = rememberScrollState()




    var context = LocalContext.current


    val sp = remember { context.getSharedPreferences(context.packageName, MODE_PRIVATE) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {



        val calendar = Calendar.getInstance()
        val dayOfMonthIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1  // 10 ise -> 9
        val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

        var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
        var firstDateInWeekCalendar by remember { mutableStateOf(calendar.timeInMillis) }
        var dayOffset by remember { mutableStateOf(calendar.timeInMillis) }
        var selectFirst by remember { mutableStateOf(true) }
        var init by remember { mutableStateOf(false) }



        val tasks = viewModel.tasks.collectAsState(initial = emptyList()).value
        val todayTasks = viewModel.todayTasks.collectAsState(initial = emptyList()).value
        val tasksByDate = viewModel.tasksByDate.collectAsState(initial = emptyList()).value

        var currentPage by remember { mutableStateOf(0) }
        val pageSize = if(10>tasksByDate.size) 10 else 7
        val pagedTasks = tasksByDate.chunked(pageSize)
        val visibleItems = remember { mutableStateListOf<Boolean>() }
        var tasksToShow = pagedTasks.take(currentPage + 1).flatten()
        var upcomingFilter by remember { mutableStateOf<(Boolean)?>(defaultTimeScope) }


        @Composable
        fun setTaskCalender(){

            Spacer(modifier = Modifier.height(16.dp))


            /*fun getDayOffset(date: Long): Int {
                //val target = Calendar.getInstance().apply { timeInMillis = date }

                val diffMillis = date - firstDateInWeekCalendar
                return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            }*/


            val title = "\n\n\uD83D\uDCCC Görev Listesi (Bugünün görevleri)       "
            Text(
                if(selectedDate == firstDateInWeekCalendar) title
                else getFormattedDate(selectedDate)?.let{"\n\n$it Görev Listesi "}?: title
                , modifier = Modifier.fillMaxWidth()
                , textAlign = TextAlign.Center,color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.headlineMedium)




            //  viewModel.todayTasks.collectAsState(initial = emptyList()).value.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


            Spacer(Modifier.height(24.dp))




            val customCategories = remember { mutableStateOf<List<String>>(emptyList()) }
            val customLocations = remember { mutableStateOf<List<String>>(emptyList()) }
            var showTimeFilterDialog by remember { mutableStateOf(false) }


            LaunchedEffect(Unit) {
                customCategories.value = TaskCategory.getCategories(sp)?.toList()?: emptyList()
                customLocations.value = getLocations(sp)?.toList()?: emptyList()


            }

            var customCat by remember { mutableStateOf<String>("") }
            var selectedLoc by remember { mutableStateOf<String>("") }
            var showSelectCustomCatDialog by remember { mutableStateOf(false) }
            var showSelectLocDialog by remember { mutableStateOf(false) }
            var searchFilterDialog by remember { mutableStateOf(false) }

            var filterValues by remember { mutableStateOf<List<String>>(listOf("","","","","","")) }
            var whereFilter by remember { mutableStateOf<((TaskEntity) -> Boolean)?>(null) }
            var orderBy by remember { mutableStateOf<((TaskEntity) -> Comparable<Any>)?>(null) }

            fun refreshTasks(){
                viewModel.getTasksByDate(selectedDate, whereFilter =  whereFilter, orderBy = orderBy, locationFilter = selectedLoc.takeIf { it!="" }
                    ,upcomingFilter = upcomingFilter)

            }

            if (showSelectCustomCatDialog) {
                SelectItemDialog(
                    "Kategori Seç",
                    items = customCategories.value,
                    onItemSelected = { selected ->
                        customCat = selected
                        refreshTasks()
                    },
                    onDismiss = { showSelectCustomCatDialog = false }
                )
            }



            if (showSelectLocDialog) {
                SelectItemDialog(
                    "Yer Seç",
                    items = customLocations.value,
                    onItemSelected = { selected ->
                        selectedLoc = selected
                        refreshTasks()
                    },
                    onDismiss = { showSelectLocDialog = false }
                )
            }

            var startTimeFilter = remember { mutableStateOf("") }
            var endTimeFilter = remember { mutableStateOf("") }
            var searchFilter = remember { mutableStateOf("") }




            val filterOptions = listOf(
                "öncelik<>"
                ,"0-${TaskPriority.HIGH.ordinal}-Yüksek"
                ,"0-${TaskPriority.MEDIUM.ordinal}-Normal"
                ,"0-${TaskPriority.LOW.ordinal}-Düşük"
                ,"durum<>"
                ,"1-${TaskStatus.COMPLETED.ordinal}-Tamamlanmış"
                ,"1-${TaskStatus.IN_PROGRESS.ordinal}-Devam Ediyor"
                ,"1-${TaskStatus.NOT_STARTED.ordinal}-Başlanmadı"
                ,"tekrar<>"
                ,"2-${TaskRepetition.NONE.ordinal}-Tekrar Yok"
                ,"2-${TaskRepetition.DAILY.ordinal}-Günlük"
                ,"2-${TaskRepetition.WEEKLY.ordinal}-Haftalık"
                ,"2-${TaskRepetition.MONTHLY.ordinal}-Aylık"
                ,"bildirim<>"
                ,"3-${true}-Açık"
                ,"3-${false}-Kapalı"
                ,"kategori<>"
                ,"4-${TaskCategory.DAILY.ordinal}-Günlük"
                ,"4-${TaskCategory.WEEKLY.ordinal}-Haftalık"
                ,"4-${TaskCategory.CUSTOM.ordinal}-Seç{}"
                ,"Yer<>"
                ,"5-${true}-Seç{y}"
                ,"     Tamam"
            )


            fun setWhereFilter(){
                whereFilter = {
                    (filterValues[0].takeIf { it!="" }?.let{it1->it.priority == it1.toInt() }?: true) &&
                            (filterValues[1].takeIf { it!="" }?.let{it1->it.status == TaskStatus.entries.get(it1.toInt()) }?: true) &&
                            (filterValues[2].takeIf { it!="" }?.let{it1->it.repetition == TaskRepetition.entries.get(it1.toInt()) }?: true) &&
                            (filterValues[3].takeIf { it!="" }?.let{it1->it.notification == it1.toBoolean() }?: true) &&
                            (filterValues[4].takeIf { it!="" }?.let{it1->
                                val cat = TaskCategory.entries.get(it1.toInt())
                                if(cat == TaskCategory.CUSTOM) it.customCategoryDetail == customCat else it.category == cat
                            }?: true) &&


                            ( if(startTimeFilter.value!="" && it.time?.contains(":")==true){
                                val min = it.time.toString().substringAfter(":").toInt()
                                val hour = it.time.toString().substringBefore(":").toInt()

                                val targetMin = startTimeFilter.value.substringAfter(":").toInt()
                                val targetHour = startTimeFilter.value.substringBefore(":").toInt()


                                (hour>targetHour) || ((hour == targetHour) && (min >= targetMin))

                            }else true) &&


                            ( if(endTimeFilter.value!="" && it.time?.contains(":")==true){
                                val min = it.time.toString().substringAfter(":").toInt()
                                val hour = it.time.toString().substringBefore(":").toInt()

                                val targetMin = endTimeFilter.value.substringAfter(":").toInt()
                                val targetHour = endTimeFilter.value.substringBefore(":").toInt()


                                (hour<targetHour) || ((hour == targetHour) && (min < targetMin))

                            }else true) &&


                            (if(searchFilter.value!=""){
                                it.title.contains(searchFilter.value)
                            }else true)


                }




                refreshTasks()
            }


            fun selectDate(date: Long){
                currentPage = 0
                selectedDate = date

                if(selectedDate != firstDateInWeekCalendar && !selectFirst){
                    upcomingFilter = null
                }else{
                    upcomingFilter = defaultTimeScope
                }


                if(orderBy==null && defaultTaskOrder == false){
                    orderBy = {
                        (-1 * (it.priority)) as Comparable<Any>
                    }
                }



                dayOffset = (selectedDate - firstDateInWeekCalendar) / (1000 * 60 * 60 * 24)

                viewModel.getTasksByDate(selectedDate, whereFilter =  whereFilter, orderBy = orderBy, locationFilter = selectedLoc.takeIf { it!="" }
                    ,upcomingFilter = upcomingFilter)

                /*
                (it.time ?: 0L) as Comparable<Any>
                 */
            }





            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    selectDate(calendar.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )




            if(searchFilterDialog){
                var text by remember { mutableStateOf("${searchFilter.value}") }
                AlertDialog(
                    onDismissRequest = {
                        searchFilterDialog = false
                    },
                    title = {
                        Text( "Ara: ", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary)
                    },
                    text = {
                        Column {
                            Row(Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)) {

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
                    },
                    confirmButton = {

                        TextButton(
                            onClick = {
                                searchFilter.value = text
                                searchFilterDialog=false
                                setWhereFilter()
                            },
                            enabled = text.isNotEmpty()

                        ) {
                            Text("TAMAM", color = MaterialTheme.colorScheme.onSecondary)
                        }

                    },
                    containerColor = if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else  Color.DarkGray
                )
            }







            var startOptionChecked by remember { mutableStateOf(startTimeFilter.value!="") }
            var endOptionChecked by remember { mutableStateOf(startTimeFilter.value!="") }

            val startPickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->

                    startTimeFilter.value = "$hourOfDay:$minute"
                    if(!startOptionChecked) startOptionChecked=true

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            val endPickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->

                    endTimeFilter.value = "$hourOfDay:$minute"
                    if(!endOptionChecked) endOptionChecked=true

                },
                23,
                59,
                true
            )







            if(showTimeFilterDialog){
                AlertDialog(
                    onDismissRequest = {
                        showTimeFilterDialog = false
                    },
                    title = {
                        Text(text = "Başlangıç bitiş saatlerini seç\n\n${startTimeFilter.value.takeIf { it!="" }?: "-"} -> ${endTimeFilter.value.takeIf { it!="" }?: "-"}", color = MaterialTheme.colorScheme.onSecondary)
                    },
                    confirmButton = {
                        Column(Modifier.fillMaxWidth()) {

                            Row(Modifier.fillMaxWidth()){

                                TextButton(onClick = {

                                    startPickerDialog.show()

                                }) {


                                    Checkbox(startOptionChecked,{
                                        startOptionChecked = it

                                        if(!it){
                                            startTimeFilter.value = ""
                                        }

                                    })

                                    Text(text = "   Başlangıç ", color = MaterialTheme.colorScheme.onSecondary)
                                }

                                Spacer(Modifier.width(8.dp))

                                TextButton(onClick = {

                                    endPickerDialog.show()

                                }) {

                                    Checkbox(endOptionChecked,{
                                        endOptionChecked = it

                                        if(!it){
                                            endTimeFilter.value = ""
                                        }

                                    })

                                    Text(text = "   Bitiş ", color = MaterialTheme.colorScheme.onSecondary)
                                }

                            }

                            Spacer(Modifier.height(8.dp))

                            Button({
                                showTimeFilterDialog = false
                                setWhereFilter()
                            }) {
                                Text("TAMAM", color = MaterialTheme.colorScheme.onSecondary)
                            }

                        }
                    }
                )
            }






            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                // Mini Takvim


                Row(Modifier.weight(9f)){
                    // Mini Takvim
                    WeekCalendar(selectedDate = selectedDate , firstDate = {
                        selectFirst = false

                        firstDateInWeekCalendar = it



                    },selectFirst = selectFirst, onDateSelected = {
                        selectDate(it)
                    })




                }



                Icon(Icons.Default.ArrowDropDown,"",Modifier
                    .weight(1f)
                    .clickable {
                        datePickerDialog.show()
                    } ,tint = MaterialTheme.colorScheme.onSecondary)





            }



            if(tasksByDate.isNotEmpty()){

                if(selectedDate == firstDateInWeekCalendar){
                    if(isSyncEnabled){



                        viewModel.syncForToday()


                        saveLastSYNC(sp,context)
                        isSyncEnabled = false
                    }
                }
            }






            var currentTime = timeFormat.format(Date())
            var isNotCompletedCount = tasksByDate.filter {data->
                if(!data.isCompleted){
                    data.time?.takeIf { it.contains(":") }?.let{
                        val min = it.substringAfter(":").toInt()
                        val hour = it.substringBefore(":").toInt()

                        val curHour = currentTime.substringBefore(":").toInt()
                        val curMin = currentTime.substringAfter(":").toInt()


                        curHour>hour || (curHour==hour && curMin>min)

                    }?: false
                }else false
            }.size

            Row(Modifier
                .fillMaxWidth()
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically){

                Text("${tasksByDate.size} Görev${isNotCompletedCount.takeIf { it>0 }?.let{ if(selectedDate == firstDateInWeekCalendar) "\n\n($it Geçmiş)" else null }?:""}", modifier = Modifier.weight(0.5f), color = MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.6f else 1f),
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)


                if(whereFilter!=null || (startTimeFilter.value!="" || endTimeFilter.value!="")){
                    Icon(Icons.Default.Clear,"",Modifier
                        .wrapContentWidth()
                        .clickable {
                            whereFilter = null
                            upcomingFilter = null
                            selectedLoc = ""
                            searchFilter.value = ""
                            startTimeFilter.value = ""
                            endTimeFilter.value = ""
                            refreshTasks()
                        } ,tint = Color.Red)
                }

                Button({

                    if(upcomingFilter!=true){
                        upcomingFilter = true
                    }else{
                        upcomingFilter = null
                    }


                    refreshTasks()


                },
                    Modifier
                        .width(84.dp)
                        .padding(4.dp)
                        .height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (upcomingFilter==true) {
                            if(isLightTheme()) MaterialTheme.colorScheme.onSecondary.copy( 1f) else MaterialTheme.colorScheme.onSecondary.copy( 0.6f)
                        } else {
                            if(isLightTheme()) MaterialTheme.colorScheme.onSecondary.copy(0.6f) else MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.3f else 1f)
                        },
                        contentColor = if (upcomingFilter==true) MaterialTheme.colorScheme.onSecondary else Color.Black
                    ),
                    border = if (upcomingFilter==true) BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary) else null,
                ) {
                    Text("Gelecek",color = Color.White, fontSize = 9.sp)
                }


                Button({

                    if(upcomingFilter!=false){
                        upcomingFilter = false
                    }else{
                        upcomingFilter = null
                    }


                    refreshTasks()



                },
                    Modifier
                        .width(84.dp)
                        .padding(4.dp)
                        .height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (upcomingFilter==false) {
                            if(isLightTheme()) MaterialTheme.colorScheme.onSecondary.copy( 1f) else MaterialTheme.colorScheme.onSecondary.copy( 0.6f)
                        } else {
                            if(isLightTheme()) MaterialTheme.colorScheme.onSecondary.copy(0.6f) else MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.3f else 1f)
                        },
                        contentColor = if (upcomingFilter==false) MaterialTheme.colorScheme.onSecondary else Color.Black
                    ),
                    border = if (upcomingFilter==false) BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary) else null,
                ) {
                    Text("Geçmiş",color = Color.White, fontSize = 9.sp)
                }

                Box(Modifier
                    .weight(1.2f)
                    .padding(8.dp)){

                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterEnd)

                        ,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {


                        var expanded by remember { mutableStateOf(false) }
                        var expanded2 by remember { mutableStateOf(false) }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            ,contentAlignment = Alignment.CenterEnd
                        ){

                            Row(modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End){





                                Spacer(modifier = Modifier.width(8.dp))

                                Image(Icons.Default.Search,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .width(24.dp)
                                        .clickable {
                                            searchFilterDialog = true
                                        }, alpha = if(searchFilter.value=="")0.4f else 1f
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Image(Icons.Default.Timer,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .width(24.dp)
                                        .clickable {
                                            showTimeFilterDialog = true
                                        }, alpha = if( startTimeFilter.value == "" && endTimeFilter.value=="")0.4f else 1f
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Image(Icons.Default.FilterAlt,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .width(40.dp)
                                        .clickable {
                                            expanded = true
                                        }, alpha = if(whereFilter==null)0.4f else 1f
                                )
                                Spacer(modifier = Modifier.width(8.dp))


                                Image(Icons.Default.FilterList,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                    modifier = Modifier
                                        .width(40.dp)
                                        .clickable {
                                            expanded2 = true
                                        }
                                )
                                Spacer(modifier = Modifier.height(16.dp))


                                DropdownMenu(
                                    expanded = expanded, modifier = Modifier.sizeIn(maxHeight = 400.dp),
                                    onDismissRequest = { expanded = false }
                                ) {
                                    filterOptions
                                        .forEachIndexed { index, s ->


                                            fun selectCat(){
                                                showSelectCustomCatDialog = true
                                            }

                                            fun selectLoc(){
                                                showSelectLocDialog = true
                                            }


                                            val listText = s.split("-").last()


                                            DropdownMenuItem(
                                                text = {

                                                    Column(Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            if (isLightTheme() && ((s.contains(
                                                                    "-"
                                                                ) && !s.contains("<>")))
                                                            ) Color.LightGray else Color.Transparent
                                                        ), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                                                            if(s.contains("-") && !s.contains("<>")){


                                                                val listIx = s.split("-").first().toInt()
                                                                val listVal = s.split("-").get(1)


                                                                val list = filterValues.toMutableList()





                                                                val isChecked = list[listIx] == listVal
                                                                Checkbox(checked = isChecked, onCheckedChange = {

                                                                    list[listIx] = if(isChecked) "" else listVal
                                                                    filterValues = list
                                                                    setWhereFilter()

                                                                    if( !isChecked && listText.contains("{}")){
                                                                        selectCat()
                                                                    }


                                                                    if( !isChecked && listText.contains("{y}")){
                                                                        selectLoc()
                                                                    }else if(isChecked && listText.contains("{y}")){
                                                                        selectedLoc = ""
                                                                    }

                                                                    //expanded = false

                                                                }, modifier = Modifier.background(Color.Transparent))
                                                                Spacer(Modifier.width(4.dp))

                                                            }else{
                                                                //setWhereFilter()
                                                            }

                                                            Text(listText.
                                                            replace("<>","").
                                                            replace("{}",customCat.takeIf { it!="" }?.let{" ($it)"}?:"").
                                                            replace("{y}",selectedLoc.takeIf { it!="" }?.let{" ($it)"}?:""),
                                                                color = MaterialTheme.colorScheme.onSecondary)

                                                        }
                                                        if (s.contains("<>")){
                                                            Spacer(Modifier.height(4.dp))
                                                            Divider(Modifier.padding(horizontal = 4.dp))
                                                            Spacer(Modifier.height(4.dp))
                                                        }
                                                    }



                                                },
                                                onClick = {


                                                    if(listText.contains("{}")){
                                                        selectCat()
                                                    } else if(listText.contains("{y}")){
                                                        selectLoc()
                                                    }else{
                                                        if(!s.contains("-") && !s.contains("<>")) {
                                                            setWhereFilter()
                                                        }
                                                        expanded = false
                                                    }

                                                }
                                            )
                                        }
                                }


                                DropdownMenu(
                                    expanded = expanded2, modifier = Modifier.sizeIn(maxHeight = 400.dp),
                                    onDismissRequest = { expanded2 = false }
                                ) {
                                    listOf("Zaman","Zaman (azalan)","Öncelik","Öncelik (azalan)","varsayılan")
                                        .forEachIndexed { index, s ->

                                            DropdownMenuItem(
                                                text = {
                                                    Text(s, color = MaterialTheme.colorScheme.onSecondary) },
                                                onClick = {

                                                    when(index){
                                                        0->{

                                                            orderBy = {
                                                                ((timeFormat.parse(it.time ?: "23:59"))?.time?: 0L) as Comparable<Any>
                                                            }

                                                        }
                                                        1->{

                                                            orderBy = {
                                                                (-1 * ((timeFormat.parse(it.time ?: "23:59"))?.time?: 0L)) as Comparable<Any>
                                                            }

                                                        }
                                                        2->{

                                                            orderBy = {
                                                                (-1 * (it.priority)) as Comparable<Any>
                                                            }

                                                        }
                                                        3->{

                                                            orderBy = {
                                                                ((it.priority)) as Comparable<Any>
                                                            }

                                                        }
                                                        else -> orderBy = null
                                                    }

                                                    refreshTasks()
                                                    expanded2 = false

                                                }
                                            )
                                        }
                                }




                            }

                        }


                    }
                }


            }


            Spacer(modifier = Modifier.height(8.dp))


            LaunchedEffect(currentPage, pagedTasks) {
                // visibleItems.clear()
                pagedTasks.getOrNull(currentPage)?.forEachIndexed { index, _ ->
                    if(isEnabledAnimation) delay(100)
                    visibleItems.add(true)
                }
            }

            // Görevler




            var completedCount by remember { mutableStateOf(0) }
            var unCompletedCount by remember { mutableStateOf(0) }

            tasksToShow.forEachIndexed { index, task ->
                AnimatedVisibility(
                    visible = visibleItems.getOrNull(index) == true,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(500)
                    ),
                    exit = fadeOut(tween(300)) + slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(300)
                    )
                ) {
                    TaskCard(
                        task = task,
                        index,
                        onClick = {
                            navController.navigate("task_details/${it}/${
                                dayOffset
                            }")
                        },
                        editTask = {task->
                            var s =
                                task!!.category!!.ordinal.toString() + "," +
                                        task!!.status!!.ordinal + "," +
                                        task!!.priority!!.ordinal + "," +
                                        task!!.repetition!!.ordinal + ","
                            task!!.customCategoryDetail
                            navController.navigate("add_task/${task.id}/$s")
                        },
                        delTask = {
                            viewModel.delTask(it!!.toEntity())
                            val notificationManager =
                                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(it.id)
                            NotificationUtils.cancelScheduledNotification(context,it.id)

                            notViewModel?.deleteNotificationsByTaskId(it.id)
                            refreshTasks()
                        },
                        onCompleteToggle = {

                            var status = task.status.ordinal
                            var newStatus = TaskStatus.entries.get((status + 1).let{
                                if(TaskStatus.entries.size>it) it else 0
                            })

                            var newCheck = newStatus == TaskStatus.COMPLETED

                            val newTask = task.copy(isCompleted = newCheck,
                                status = newStatus/*if(newCheck) TaskStatus.COMPLETED else TaskStatus.NOT_STARTED*/
                                ,   lastCompletedDate = (dateFormat3.format(Date())).takeIf { newCheck})

                            viewModel.updateTask(newTask.toEntity())

                            if(newCheck){
                                val notificationManager =
                                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancel(task.id)

                                NotificationUtils.cancelScheduledNotification(context,task.id)
                                if(task.notification) notViewModel?.deleteNotificationsByTaskId(task.id)
                            }else{

                                if(task.notification) NotificationUtils.addNotificationToDatabase(context,task)

                            }


                            //currentPage = 0

                            dayOffset = (selectedDate - firstDateInWeekCalendar) / (1000 * 60 * 60 * 24)

                            viewModel.getTasksByDate(selectedDate,  whereFilter =  whereFilter, orderBy = orderBy, locationFilter = selectedLoc.takeIf { it!="" }
                                ,upcomingFilter = upcomingFilter)

                        },
                        dayOffset = dayOffset,
                        dayOfMonth = dayOfMonthIndex+1
                    )
                }
            }
            if (currentPage + 1 < pagedTasks.size) {
                val remainingTasks = tasksByDate.size - ((currentPage + 1) * pageSize)

                Button(
                    onClick = { currentPage++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF444444)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("($remainingTasks) görev daha", color = if(isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                    }
                }
            }
            if (currentPage + 1 >= pagedTasks.size) {
                //Tüm görevler görüntülendi
                Text(
                    if(tasksByDate.size>0)"${tasksByDate.filter { it.isCompleted }.size}/${tasksByDate.size} görev tamamlandı"
                    else if(selectedDate == firstDateInWeekCalendar) {

                        if(whereFilter!=null)
                            "Hiç Görev Yok" else

                            "Bugün hiç tamamlanmamış görev yok"} else "Hiç görev yok",
                    color = if(isLightTheme()) Color.DarkGray else Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }


        }



        val todayTotalProgress = remember { mutableStateOf(CountsResult(0,0)) }



        // Alışkanlıklar
        @Composable
        fun setHabbits(){
            Spacer(modifier = Modifier.height(16.dp))

            val isSelectedNotCompleted = habitViewModel.filter.value == "show is not completed"

            val totalHabits = remember { mutableStateOf<MutableSet<HabitEntity>>(mutableSetOf()) }

            val habits by habitViewModel.paginatedHabitsForToday.collectAsState()
            val currentPage by habitViewModel.currentPage.collectAsState()
            val totalCount by habitViewModel.totalCount.collectAsState()




            HabitSection(totalHabits.value.map { it.toDomain() },
                clickHabit={
                    navController.navigate("habit_details/$it")
                }, onHabitToggle = {
                habitViewModel.toggleHabit(it.toEntity(),context,loadHabitChecks = true,All = false)
            },totalCount = totalCount,pageSize = habitViewModel.pageSize
                , totalProgress = todayTotalProgress.value,
                delHabit = {
                habitViewModel.delHabit(it,context,All = false)
            }, editHabit = {
                navController.navigate("add_habit/$it")
            },activeToggle = {
                habitViewModel.activeToggleHabit(it,All = false)
            }, thisIsChecked ={
                habitChecks.get(it.id)?.isChecked == true
            },filter = {
                habitViewModel.filter.value = it
                habitViewModel.loadHabits( All = false)
            },isSelectedNotCompleted = isSelectedNotCompleted,
                beforePage = {
                    totalHabits.value = mutableSetOf()
                    habitViewModel.previousPage(loadHabitChecks = true,All = false)
                },
                nextPage = {
                    totalHabits.value = mutableSetOf()
                    habitViewModel.nextPage(loadHabitChecks = true,All = false)
                },
                loadMore = {
                    habitViewModel.nextPage(loadHabitChecks = true,All = false)
                }
            )


            /*LaunchedEffect(habits) {
                habitViewModel.loadHabits(loadHabitChecks = true)
            }*/

        /*    LaunchedEffect(habitChecks) {
                habitViewModel.loadHabitChecks()
            }*/
            LaunchedEffect(Unit) {
                if(habitViewModel._currentPage.value!=0) habitViewModel._currentPage.value = 0
                if(!habitViewModel._isActive.value) habitViewModel._isActive.value = true
                habitViewModel.loadHabits(loadHabitChecks = true,getTotalCount = true, All = false)
            }

            habits.let {
                val before = totalHabits.value
                before.addAll(it)
                totalHabits.value = before
            }

        }

        // İstatistik kartı
        @Composable
        fun setStats(){
            Spacer(modifier = Modifier.height(16.dp))

            StatsCard(tasksByDate,selectedDate,{
            }){

                navController.navigate("stats")

            }
        }









        LaunchedEffect(Unit) {

          //  habitViewModel.loadHabitChecks()

          //  habitViewModel.loadHabits(loadHabitChecks = true)
            val (dayOfYear, year) = getDayOfYearAndDaYear()
            val calendar =Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
            }

            val dayOfWeek = ((android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
            val dayOfMonth = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_MONTH)   // 1-31
            todayTotalProgress.value = habitViewModel.getTodayTotalProgress("-$dayOfWeek-","->$dayOfMonth<-",dayOfYear, year)

        }


        homeScreenLayout.split(",").forEach {
            it.trim().let{
                when(it){
                    "İstatistik"->setStats()
                    "Alışkanlık listesi"->setHabbits()
                    else->setTaskCalender()
                }
            }
        }

        Spacer(Modifier.height(60.dp))






    }



    // FAB
    var showEnableNotDialog = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            SmallFloatingActionButton(
                onClick = {

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1 // İzin isteği kodu
                        )
                    }else {
                        sp.getInt("reminderOffsetMinutes",-1).takeIf { it>-1 }.let{v->
                            if(v==null){
                                showEnableNotDialog.value = true
                            }else{
                                navController.navigate("add_task/true")
                            }
                        }
                    }

                },
                containerColor = if (isLightTheme()) Color.Black
                else androidx.compose.material3.FloatingActionButtonDefaults.containerColor
            ) {
                Icon(Icons.Default.NotificationAdd, contentDescription = "Bildirim Ekle",
                    tint = if (isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary,)
            }

            FloatingActionButton(
                onClick = { navController.navigate("add_task") },
                containerColor = if (isLightTheme()) Color.Black
                else androidx.compose.material3.FloatingActionButtonDefaults.containerColor
            ) {
                Icon(
                    Icons.Default.Add,
                    tint = if (isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary,
                    contentDescription = "Görev Ekle"
                )
            }
        }
    }


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




}
@Composable
fun SelectItemDialog(
    title: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, color = MaterialTheme.colorScheme.onSecondary)
        },
        text = {
            Column {
                items.forEach { item ->
                    Text(
                        text = item, color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(item)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}



/*
"""
    id = ${task.id}
    title = ${task.title}
    description = ${task.description ?: "Null"}
    days = ${task.days}
    date = ${task.date}
    time = ${task.time ?: "Null"}
    category = ${task.category}
    customCategoryDetail = ${task.customCategoryDetail ?: "Null"}
    status = ${task.status}
    priority = ${task.priority}
    notification = ${task.notification}
    repetition = ${task.repetition ?: "Null"}
    locations = ${task.locations ?: "Null"}
    subTasks = ${task.subTasks ?: "Null"}
    color = ${task.color ?: "Null"}
    attachments = ${task.attachments ?: "Null"}
    isCompleted = ${task.isCompleted}
""".trimIndent()
 */


/*
@Composable
fun HomeScreenContent(viewModel: TaskViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ana Sayfa") },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Görev Ekleme Sayfasına Git */ },
                backgroundColor = Color.Blue
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab, backgroundColor = MaterialTheme.colors.primary) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Görevler") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Alışkanlıklar") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("İstatistik") })
            }

            when (selectedTab) {
                0 -> TaskList(viewModel)
                1 -> HabitsList(viewModel)
                2 -> StatsScreen()
            }
        }
    }
}

@Composable
fun TaskList(viewModel: TaskViewModel) {
    val tasks = viewModel.getDailyTasks().collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(tasks.value) { task ->
            TaskCard(task)
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.description, style = MaterialTheme.typography.body2)
            }
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { /* Update task completion status */ }
            )
        }
    }
}*/