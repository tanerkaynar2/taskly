package com.taner.taskly.presentation.ui.components

import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.data.local.entity.HabitCheckEntity
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.domain.model.HabitFrequency
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.habitNotCheckChecksByDayOfYearAndYear
import java.util.Calendar

@Composable
fun HabitTracking(
    dayOfYearsAndYearList: List<Pair<Int, Int>>,
    habits: List<Habit> = emptyList(),
    clickHabit: (Int) -> Unit,
    editHabit: (Int) -> Unit,
    delHabit: (Int) -> Unit,
    onHabitToggle: (Habit,Int,Int,Boolean) -> Unit,
    activeToggle: (Int) -> Unit,
    totalCount: (Int),
    pageSize: (Int),
    totalProgress: CountsResult,
    thisIsChecked: (Pair<Int, Pair<Int,Int>>)->Boolean,
    isSelectedNotCompleted: Boolean = false,
    nextPage: ()->Unit,
    loadMore: ()->Unit,
    beforePage: ()->Unit,
    refresh: ()->Unit,
    onChangeDayCount: ()->Unit,
    filter: (String?)->Unit
) {
    val scrollState = rememberScrollState()


    val colors = listOf(
        Color(0xFF121212), // Koyu siyah (Night Black)
        Color(0xFF1E1E1E)  // Hafif gri-siyah (Charcoal Black)
    )

    val c = LocalContext.current


    val dayCal = Calendar.getInstance()
    val days = dayOfYearsAndYearList.map { it.let {(dayOfYear,year)->
        dayCal.set(Calendar.DAY_OF_YEAR,dayOfYear)
        dayCal.set(Calendar.YEAR,year)

        dayCal.get(Calendar.DAY_OF_MONTH).toString() + "\n" + monthName(dayCal.get(Calendar.MONTH)+1)
    } }




    val boxSize = 40.dp

    Row(Modifier
        .fillMaxSize()
        /*.padding(8.dp)*/) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height( boxSize + 8.5.dp))

            /*
            Image(/*Icons.Default.Refresh*/,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                modifier = Modifier
                    .size(boxSize - 10.dp)
                    .clickable {
                        refresh.invoke()
                    }
                    .padding(6.dp))*/



            habits.forEachIndexed { index, habit ->

                Box(Modifier.fillMaxWidth().height(boxSize).background(colors[index%2]), contentAlignment = Alignment.Center){

                    Text(text = habit.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary
                        , textAlign = TextAlign.Center, modifier = Modifier
                            //.fillMaxSize()
                            .clickable { clickHabit.invoke(habit.id) }
                        , fontSize = 12.sp)
                }

               // Divider()
            }
        }


        //Spacer(modifier = Modifier.width(8.dp))


        Column(
            modifier = Modifier
                .weight(6f)
                .horizontalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier
                            .padding(/*horizontal = 4.dp*/)
                            .width(boxSize/* - 8.dp*/)
                            .height(boxSize),
                        fontSize = 12.sp, textAlign = TextAlign.Center,
                        color = if (isLightTheme()) Color.Black else Color.White
                    )
                }

                Text("",
                    modifier = Modifier
                        .padding(/*horizontal = 4.dp*/)
                        .width(boxSize/* - 8.dp*/)
                        .height(boxSize))

                Image(Icons.Default.Visibility,"",modifier = Modifier
                    .width(boxSize/* - 8.dp*/).height(boxSize)
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp/*horizontal = 4.dp*/)
                    .clickable { onChangeDayCount.invoke() }, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary))


            }
            Spacer(modifier = Modifier.height(8.dp))






            Column {
                habits.forEachIndexed { index, habit ->

                    val habitCal = Calendar.getInstance().apply {
                        timeInMillis=habit.createdAt
                    }
                    val habitCreDayOfYear = habitCal.get(Calendar.DAY_OF_YEAR)
                    val habitCreYear = habitCal.get(Calendar.YEAR)


                    val freqType = when(habit.frequency){
                        HabitFrequency.DAILY->0
                        HabitFrequency.WEEKLY->1
                        else->2
                    }

                    Row(Modifier
                        .background(colors[index%2])
                    ) {
                        dayOfYearsAndYearList.forEach { (dayOfYear, year)->
                            var a = !thisIsChecked(habit.id to (dayOfYear to year))
                            val isChecked = remember { mutableStateOf(a) }

                            fun thisTimeToday():Boolean{
                                if(freqType==0) return true

                                dayCal.set(Calendar.DAY_OF_YEAR,dayOfYear)
                                dayCal.set(Calendar.YEAR,year)

                                return habit.days.contains(if(freqType==1){
                                   dayCal.get(Calendar.DAY_OF_WEEK).let {
                                        if (it == 1) 7 else it - 1
                                    }
                                }else{
                                    dayCal.get(Calendar.DAY_OF_MONTH)
                                })

                            }

                            if(
                                (year>habitCreYear || (year==habitCreYear && dayOfYear>=habitCreDayOfYear)) &&
                                thisTimeToday()
                            ){
                                Checkbox(
                                    checked = !isChecked.value,
                                    onCheckedChange = {
                                        isChecked.value = !isChecked.value
                                        onHabitToggle.invoke(habit,dayOfYear,year,isChecked.value)
                                        /* val before = isCheckedList.toMutableSet()
                                         if(isChecked){
                                             before.remove((dayOfYear to year) to habit.id)
                                         }else{
                                             before.add((dayOfYear to year) to habit.id)
                                         }
                                         isCheckedList = before*/

                                    },
                                    modifier = Modifier
                                        .padding(/*horizontal = 4.dp*/)
                                        .width(boxSize/* - 8.dp*/)
                                        .height(boxSize)
                                )
                            }else{

                                Text("",
                                    modifier = Modifier
                                        .padding(/*horizontal = 4.dp*/)
                                        .width(boxSize/* - 8.dp*/)
                                        .height(boxSize))

                            }

                        }



                        Text("",
                            modifier = Modifier
                                .padding(/*horizontal = 4.dp*/)
                                .width(boxSize/* - 8.dp*/)
                                .height(boxSize))
                        Text("",
                            modifier = Modifier
                                .padding(/*horizontal = 4.dp*/)
                                .width(boxSize/* - 8.dp*/)
                                .height(boxSize))

                    }

                }
            }





        }
    }
}
