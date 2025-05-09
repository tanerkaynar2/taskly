package com.taner.taskly.presentation.ui.screen.habits


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.ui.components.HabitCard
import com.taner.taskly.presentation.ui.theme.TasklyTheme
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.isInitHabitSection
import com.taner.taskly.presentation.viewmodel.TaskViewModel



@Composable
fun HabitsScreen(
    navController: NavController,
    viewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
){


    LaunchedEffect(Unit) {
        if(viewModel._currentPage.value!=0) viewModel._currentPage.value = 0
        if(!isInitHabitSection){
            isInitHabitSection = true}

        viewModel.loadHabits(loadHabitChecks = true)

    }


    val totalCount by viewModel.totalCount.collectAsState()


    val filterData = remember { mutableStateOf(FilterData(
        isActive = true,
        isNotCompleted = viewModel.filter.value == "show is not completed"
    )) }
    fun getTitleText(): String {
        return (
                if(filterData.value.isActive) "" else "Pasif " +
                if(filterData.value.isNotCompleted) "Yapılmamış " else ""
                ) + "Alışkanlıklar"
    }
    val titleText = remember { mutableStateOf(getTitleText()) }

    Column(modifier = Modifier
        .fillMaxSize()
    ) {


        Row(Modifier.fillMaxWidth()
            .padding(16.dp)
        , verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

            Box(Modifier.weight(1f).padding(4.dp)){
                /*Image(Icons.Default.AddLocationAlt,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterEnd)
                )*/

                StylishButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (filterData.value.isActive) "Aktif" else "Pasif",
                    filterData = filterData,
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val newValue =  !filterData.value.isActive
                    filterData.value = filterData.value.copy(isActive =newValue)
                    viewModel._isActive.value = newValue
                    viewModel.loadHabits()
                    titleText.value = getTitleText()

                }


            }

            Box(Modifier.weight(1f).padding(4.dp)){


                StylishButton(
                    modifier = Modifier.fillMaxWidth(),
                    text =  if(filterData.value.isNotCompleted) "Yapılmamışlar" else "Tümü",
                    filterData = filterData,
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val newValue =  !filterData.value.isNotCompleted
                    filterData.value = filterData.value.copy(isNotCompleted =newValue)
                    viewModel.filter.value = if(newValue) "show is not completed" else "all"
                    viewModel.loadHabits(loadHabitChecks = true)
                    titleText.value = getTitleText()
                }


            }

        }


        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {



            LaunchedEffect(Unit) {
                viewModel.loadHabits()
            }

            if(totalCount==0){
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Henüz hiç alışkanlık eklemediniz.",color = MaterialTheme.colorScheme.onSecondary)
                }
            }else{
                HabitListScreen(titleText,viewModel,habitCheckViewModel,navController)
            }

        }
    }










    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { navController.navigate("add_habit") },
            containerColor = if (isLightTheme()) Color.Black
            else androidx.compose.material3.FloatingActionButtonDefaults.containerColor
        ) {
            Icon(
                Icons.Default.Add,
                tint = if (isLightTheme()) Color.White else MaterialTheme.colorScheme.onSecondary,
                contentDescription = "Alışkanlık Ekle"
            )
        }
    }




}

data class FilterData(
    var isActive: Boolean = true,
    var isNotCompleted: Boolean = true
)

@Composable
fun StylishButton(
    modifier: Modifier,
    text: String,
    filterData: MutableState<FilterData>?=null,
    isActiveState: MutableState<Boolean>?=null,
    activeColor: Color = MaterialTheme.colorScheme.onSecondary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    fontSize: TextUnit=15.sp,
    roundedCornerShape: Dp =12.dp,
    onClick: () -> Unit
) {
    val backgroundColor = if (
        ((filterData?.value?.let{it.isActive})
            ?: isActiveState?.value?: true) )
        activeColor else inactiveColor
    val contentColor = if (
        ((filterData?.value?.let{it.isActive})
            ?: isActiveState?.value?: true)
    ) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    TextButton(
        onClick = {
            onClick()
        },
        modifier = modifier
            .clip(RoundedCornerShape(roundedCornerShape))
            .background(backgroundColor)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun HabitListScreen(text: MutableState<String>,habitViewModel: HabitViewModel, habitCheckViewModel: HabitCheckViewModel, navController: NavController) {
    val habits by habitViewModel.paginatedHabits.collectAsState()
    val currentPage by habitViewModel.currentPage.collectAsState()
    val totalCount by habitViewModel.totalCount.collectAsState()



        fun calcTotalPage(totalCount: Int, pageSize: Int): Int{
            val totalPages = (totalCount + pageSize - 1) / pageSize
            return totalPages
        }


    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Başlık
        Text(
            text = text.value + " (${habits.size} / $totalCount Gösteriliyor)",
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(16.dp)
        )

        // Column ile sayfalı liste
        Column(modifier = Modifier.fillMaxSize()) {
            // Her bir habit için kart ekliyoruz
            habits.forEachIndexed { index,habit ->
                HabitCard(habit.toDomain(),index, onClick = {

                    navController.navigate("habit_details/$it")

                }, habitSuccessRate = {
                    habitCheckViewModel.calculateSuccessRate(habit.id)
                }, delHabit = {
                    habitViewModel.delHabit(it.toEntity(),context,All = true)
                }, editHabit = {
                    navController.navigate("add_habit/${it.id}")
                }, activeToggleHabit = {
                    habitViewModel.activeToggleHabit(it,All = true)
                })
            }
        }





        // Sayfa kontrol butonları
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            if(currentPage>0){
                Button(
                    onClick = { habitViewModel.previousPage(All = true) },
                    enabled = currentPage > 0,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Önceki Sayfa",color = MaterialTheme.colorScheme.onSecondary)
                }
            }

            Text("\n${currentPage+1}/${calcTotalPage(totalCount,habitViewModel.pageSize)}",color = MaterialTheme.colorScheme.onSecondary,  modifier = Modifier.padding(8.dp))


            if(calcTotalPage(totalCount, habitViewModel.pageSize)-1>currentPage){
                Button(
                    onClick = { habitViewModel.nextPage(All = true) },
                    enabled = true,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Sonraki Sayfa",color = MaterialTheme.colorScheme.onSecondary)
                }
            }

        }
    }
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