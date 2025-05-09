package com.taner.taskly.presentation.ui.screen.stats

import android.app.DatePickerDialog
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources.Theme
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat
import com.taner.taskly.core.utils.DateUtils.Companion.getDayOfYearAndDaYear
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.ui.components.HabitSection
import com.taner.taskly.presentation.ui.components.HabitTracking
import com.taner.taskly.presentation.ui.components.LongestStreaksCard
import com.taner.taskly.presentation.ui.components.StatItemWithProgress
import com.taner.taskly.presentation.ui.components.monthName
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfMonth
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.habitChecks
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.habitChecksByDayOfYearAndYear
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.habitNotCheckChecksByDayOfYearAndYear
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.isInitHabitSection
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.monthIndex
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.year
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun StatsScreen(
    habitCheckViewModel: HabitCheckViewModel,
    habitViewModel: HabitViewModel,
    navController: NavHostController,
    selectedTimeStamp: Long?=null
) {

    val context = LocalContext.current

    val sp = remember { context.getSharedPreferences(context.packageName, MODE_PRIVATE) }

    val dayCount = sp.getInt("stats_day_count",10)
    var showNumbDialog = remember { mutableStateOf(false) }

    if(showNumbDialog.value){
        numberPickerDialog(
            initialValue = dayCount,
            onDismiss = { showNumbDialog.value = false },
            onConfirm = { selectedNumber ->
                showNumbDialog.value = false
                sp.edit().putInt("stats_day_count",selectedNumber).apply()
                navController.navigate("stats") {
                    popUpTo("stats") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }


    val todayTotalProgress = remember { mutableStateOf(CountsResult(0,0)) }

    var addMotiveLabel = remember { mutableStateOf(false) }



    Column( modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {


            val isSelectedNotCompleted = habitViewModel.filter.value == "show is not completed"

            val totalHabits = remember { mutableStateOf<MutableSet<HabitEntity>>(mutableSetOf()) }

            val habits by habitViewModel.paginatedHabitsForToday.collectAsState()
            val currentPage by habitViewModel.currentPage.collectAsState()
            val totalCount by habitViewModel.totalCount.collectAsState()

            val dayCal = Calendar.getInstance().apply { timeInMillis=System.currentTimeMillis() }

            var text = if(totalCount>0){
                if(selectedTimeStamp == null)
                    "Bu günün Görevleri" else {
                    dateFormat.format(Date(dayCal.timeInMillis)) + " Görevleri"
                }
            }else "Henüz hiç alışkanlık eklemediniz."

            val dayOfYears = mutableListOf<Pair<Int, Int>>()
            (0..dayCount).forEach {

                dayOfYears.add(dayCal.get(Calendar.DAY_OF_YEAR) to dayCal.get(Calendar.YEAR))

                dayCal.add(Calendar.DAY_OF_YEAR,-1)

            }

            var refresh = remember { mutableStateOf(1) }





            LaunchedEffect(Unit) {
                if(habitViewModel._currentPage.value!=0) habitViewModel._currentPage.value = 0
                if(!habitViewModel._isActive.value) habitViewModel._isActive.value = true
                habitViewModel.loadHabits(
                    // timeStamp = selectedTimeStamp

                    loadHabitChecksByDayOfYear = dayOfYears,getTotalCount = true, All = false)

                habits.let {
                    val before = totalHabits.value
                    before.addAll(it)
                    totalHabits.value = before
                }

            }


            LaunchedEffect(Unit) {
                refresh.value+=1
            }

            LaunchedEffect(habitViewModel.isLoadedHabitChecksByDayOfYear) {
                if(habitViewModel.isLoadedHabitChecksByDayOfYear==true) refresh.value+=1
            }

            if(refresh.value>0){

                HabitTracking (
                    dayOfYears,totalHabits.value.map { it.toDomain() },
                    clickHabit={
                        navController.navigate("habit_details/$it")
                    }, onHabitToggle = {it,dayOfYear,year,isCheck->

                        habitViewModel.toggleHabit(it.toEntity(),context,/*loadHabitChecks = true,*/
                            All = false,dayOfYear=dayOfYear, year=year,loadHabitChecksByDayOfYearAndYear = true)


                    },totalCount = totalCount,pageSize = habitViewModel.pageSize
                    , totalProgress = todayTotalProgress.value,
                    delHabit = {
                        habitViewModel.delHabit(it,context,All = false)
                    }, editHabit = {
                        navController.navigate("add_habit/$it")
                    },activeToggle = {
                        habitViewModel.activeToggleHabit(it,All = false)
                    }, thisIsChecked ={(habitId, pair)->

                        if(habitNotCheckChecksByDayOfYearAndYear[habitId]?.any { it == pair }==true) false else
                            habitChecksByDayOfYearAndYear[habitId]?.any {
                                it.isChecked && it.dayOfYear == pair.first && it.year == pair.second}==true
                    },filter = {
                        habitViewModel.filter.value = it
                        habitViewModel.loadHabits( All = false, loadHabitChecks = true)
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
                    },refresh = {
                        refresh.value+=1
                        navController.navigate("stats") {
                            popUpTo("stats") { inclusive = true }
                            launchSingleTop = true
                        }
                    },onChangeDayCount={
                        showNumbDialog.value = true
                    }
                )




                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        val selectedTimestamp = calendar.timeInMillis

                        navController.navigate("stats/$selectedTimestamp")

                    },
                    year,
                    monthIndex,
                    dayOfMonth
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("\n\n\n$text         \n\n\n",color = MaterialTheme.colorScheme.onSecondary)

                    if(text!="Henüz hiç alışkanlık eklemediniz."){
                        Icon(Icons.Default.CalendarToday,"", modifier = Modifier.clickable {


                            datePickerDialog.show()

                        },tint = MaterialTheme.colorScheme.onSecondary)
                    }

                }



            }





            val setStatsCard = remember { mutableStateOf(false) }

            if(setStatsCard.value && totalCount>0){
                Spacer(Modifier.height(24.dp))
                GeneralStatsCard(todayTotalProgress.value.totalCount,todayTotalProgress.value.completedCount)
                Spacer(Modifier.height(24.dp))
            }




            LaunchedEffect(Unit) {
                setStatsCard.value = true
            }




            val calculatedSuccessRated = remember { mutableStateOf(HashMap<Int, Pair<String, Int>>()) }




            Column(Modifier.fillMaxWidth()) {
                if(calculatedSuccessRated.value.isNotEmpty()){

                    Spacer(Modifier.height(24.dp))

                    Text(" \uD83D\uDCCA Alışkanlık İstatistikleri              \n",color=MaterialTheme.colorScheme.onSecondary)


                    fun getHabit(id: Int): HabitEntity? {
                        return habits.firstOrNull { it.id == id }
                    }



                    for((habitId,pair) in calculatedSuccessRated.value){
                        val (habitName, progress) = pair
                        StatItemWithProgress(habitName,progress,onClick={
                            navController.navigate("habit_details/$habitId")
                        })
                    }
                    Spacer(Modifier.height(24.dp))

                }

            }





            val longestStreakList = remember { mutableStateOf(listOf<Habit>()) }

            LaunchedEffect(Unit) {
                val li = hashMapOf<Int, Pair<String, Int>>()
                habits.forEach {
                    habitCheckViewModel.calculateSuccessRate(it.id).let { p->
                        li[it.id] = it.name to p
                    }
                }
                calculatedSuccessRated.value = li
            }


            if(longestStreakList.value.isNotEmpty()){

                Spacer(Modifier.height(24.dp))
                LongestStreaksCard(longestStreakList.value) {
                    navController.navigate("habit_details/${it.id}")
                }
                Spacer(Modifier.height(24.dp))

            }


            LaunchedEffect(Unit) {
                longestStreakList.value = habitViewModel.getLongestStreak()?.map { it.toDomain() }?: emptyList()
            }

            Spacer(Modifier.height(40.dp))

        }
        if(addMotiveLabel.value){
            MotiveLabel(Modifier.padding())
        }
    }



    LaunchedEffect(Unit) {

        //  habitViewModel.loadHabitChecks()

        //  habitViewModel.loadHabits(loadHabitChecks = true)
        val (dayOfYear, year) = getDayOfYearAndDaYear()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val dayOfWeek = ((android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1 //1-7
        val dayOfMonth = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.DAY_OF_MONTH)   // 1-31
        todayTotalProgress.value = habitViewModel.getTodayTotalProgress("-$dayOfWeek-","->$dayOfMonth<-",dayOfYear, year)

    }








    LaunchedEffect(Unit) {

        addMotiveLabel.value = true

        if(!isInitHabitSection){
            isInitHabitSection = true}}
}


@Composable
fun numberPickerDialog(
    initialValue: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val number = remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "7-30",
                color = if (isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onSecondary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "",
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { if (number.value > 7) number.value-- }) {
                        Text("-",color=MaterialTheme.colorScheme.onSecondary)
                    }
                    Text(
                        text = number.value.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(onClick = { if (number.value < 30) number.value++ }) {
                        Text("+",color=MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton (onClick = { onConfirm(number.value) }) {
                Text(
                    text = "Tamam",
                    color = if (isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "İptal",
                    color = if (isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        containerColor = if (isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray
    )
}




@Composable
fun GeneralStatsCard(
    totalTasks: Int,
    completedTasks: Int
) {
    val completionRate = if (totalTasks == 0) 0 else (completedTasks * 100 / totalTasks)

    val backgroundColor = when {
        completionRate == 100 -> Color(0xFF00AA06)
        completionRate >= 80 -> Color(0xFF00FF0A)
        completionRate >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFFF001A)
    }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            if(isLightTheme()) Color.Black.copy(0.7f) else Color.Gray.copy(0.7f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f).padding(4.dp)) {
                Text(
                    text = "📈 Genel İstatistikler",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Toplam Görev: $totalTasks",
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Row {

                    Text(
                        text = "• Tamamlanan Görev: ",
                        color =  MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "$completedTasks",
                        color = backgroundColor// MaterialTheme.colorScheme.onSecondary
                    )
                }
                Row {
                    Text(
                        text = "• Tamamlanma Oranı: %",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "$completionRate",
                        color = backgroundColor// MaterialTheme.colorScheme.onSecondary
                    )
                }

            }

            Column(Modifier.weight(1f).fillMaxHeight().padding(4.dp), verticalArrangement = Arrangement.Center,
                horizontalAlignment =  Alignment.CenterHorizontally) {
                Text("$completedTasks/$totalTasks", color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}
@Composable
fun MotiveLabel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    val tips = listOf(
        "💡 Not: Disiplin, süreklilikle kazanılır.",
        "🚀 Küçük adımlar, büyük sonuçlar getirir.",
        "🧠 Zihnini besle, bedenin seni takip eder.",
        "🎯 Hedefsiz bir çaba, rüzgarsız yelkendir.",
        "⏳ Sabır, gelişimin gizli silahıdır.",
        "🌱 Her gün %1 daha iyi olmak yeterli.",
        "📊 Takip etmek, farkındalık yaratır.",
        "🔥 Bugün başla, yarın şükredersin.",
        "💪 Zorlandığın yerde gelişiyorsun.",
        "🔁 Tekrar, ustalığın temelidir.",
        "🕯️ Bugünkü çaban, yarının ışığıdır.",
        "🏁 Başlamadan asla bilemezsin.",
        "🛠️ Gelişim, konfor alanının dışında başlar.",
        "📌 Ne kadar sürdüğüne değil, ne kadar vazgeçmediğine bak.",
        "⛰️ Büyük zirveler, sessiz tırmanışlar ister.",
        "🧱 Sabit bir yapı, kararlı bir zihinle inşa edilir.",
        "🌤️ Her sabah yeni bir başlangıçtır.",
        "🧭 Disiplin, motivasyonun pusulasıdır.",
        "🚦 Bekleme. Uygula. Düzelt. Devam et.",
        "🧗 En dik duvarlar, en güçlü karakteri oluşturur.",
        "📖 Bilgi uygularsan güçtür."
    )


    val randomTip = remember { tips.random() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = randomTip,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
