package com.taner.taskly.presentation.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.taner.taskly.core.utils.DateUtils.Companion.getDaysOfMonth
import com.taner.taskly.core.utils.DateUtils.Companion.monthNames
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitCalendarView(
    startDayOfYearsAndYear: Pair<Int, Int> = 0 to 0,
    modifier: Modifier = Modifier,
    todayDayOfYear: Int,
    todayYear: Int,

    habitDaysOfMonth: List<Int>?=null,
    habitDaysOfWeek: List<Int>?=null,

    monthIndexesAndYear: List<Pair<Int, Int>>,

    previousBtn:()->Unit,
    nextBtn:()->Unit,
    goToHabitStartDayBtn:()->Unit,
    goToToDayBtn:()->Unit,

                                                                        // Vurgulanacak özel günler (örn. en uzun seri günleri)
    showMonthLabels: Boolean = true,                         // Ay isimlerini göster
    loadCheckValues:(suspend (List<Pair<Int,Int>>)->(HashMap<Pair<Int, Int>, habitCalendarValues>))?=null,

    defaultColor: Color = Color.Gray.copy(0.1f),

    onBeforeStartDayClick: (Pair<Int, Int>)->Unit,
    onAfterStartDayClick: (Pair<Int, Int>)->Unit,
    onDayClick: (Pair<Pair<Int, Int>,Pair<Int, Boolean>>) -> habitCalendarValues?,
){

    val checkedDays = remember { mutableStateOf(HashMap<Pair<Int, Int>, habitCalendarValues>()) }

    val currentDayOfYearCheckValues = remember { mutableStateOf(mutableListOf<Pair<Int, Int>>()) }
    var showPopupText = remember { mutableStateOf<String?>(null) }
    var showPopup = remember { mutableStateOf(false) }
    var showPopupChangeVoid = remember { mutableStateOf<()->Unit>({}) }
    if(showPopup.value){

        showPopupText.value?.let {
            ChatBubblePopup(showPopup.value,{
                showPopupChangeVoid.value.invoke()
            },{
                showPopup.value = false
            }, it)
        }

        /*AlertDialog(
            onDismissRequest = {showPopup.value = false},
            title = {

            },
            text = {
                Text(showPopupText.value?:"", color = MaterialTheme.colorScheme.onSecondary)
            }, confirmButton = {}

        )*/
    }

    val startDayOfYear = startDayOfYearsAndYear.first
    val startYear = startDayOfYearsAndYear.second

    @Composable
    fun getDay(days: List<Int>, dayOfYears: List<Int>, years: List<Int>, dayIndexesOfWeek: List<Int>,position:Int){


        val blockRangeForDayOfYear = (startDayOfYear..todayDayOfYear)


        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalArrangement = Arrangement.Center
        ) {
            items(position){
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .background(
                           Color.Transparent
                        )
                        .padding(10.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){}
            }
            items(days.size) { index ->



                val dayOfYear = dayOfYears.get(index)
                val year = years.get(index)
                val dayOfMonth = days.get(index)
                val dayIndexOfWeek = dayIndexesOfWeek.get(index)

                val todayHabitDay = habitDaysOfMonth?.contains(dayOfMonth) ?:
                habitDaysOfWeek?.contains(dayIndexOfWeek) ?: true

                val originalHabitCalendarValues = checkedDays.value[dayOfYear to year]

                val valueIsCheckDay = remember { mutableStateOf<Boolean?>( null) }
                val valueIsMissDay =remember { mutableStateOf<Boolean?>( null) }
                val valueIsHighlightedDay = remember { mutableStateOf<Boolean?>( null) }

                var isCheckDay = originalHabitCalendarValues?.isCheckedDay == true
                var isMissDay =(!isCheckDay) && originalHabitCalendarValues?.isMissedDay == true
                var isHighlightedDay = originalHabitCalendarValues?.isHighlightedDay == true
                val highlightedDayColor = originalHabitCalendarValues?.highlightedDayColor?: Color.Transparent
                val checkDaysColor = originalHabitCalendarValues?.checkDayColor?: Color.Green.copy(0.7f)
                val missedDaysColor= originalHabitCalendarValues?.missedDayColor?: Color.Red.copy(0.4f)
                val highlightedDayIcon= originalHabitCalendarValues?.highlightedDayIcon

                val beforeCurrentDayOfYearCheckValues = currentDayOfYearCheckValues.value
                beforeCurrentDayOfYearCheckValues.add(dayOfYear to year)
                currentDayOfYearCheckValues.value = beforeCurrentDayOfYearCheckValues

                var beforeDate = startYear > year || (startYear == year  && startDayOfYear>dayOfYear)
                var block = !todayHabitDay || !(

                        if(todayYear == year && year == startYear){
                            dayOfYear in blockRangeForDayOfYear
                        }/*else if(todayYear == startYear){

                        }*/else if(year < startYear){
                            false
                        }else if(todayYear>year && year>startYear){
                            //todayYear == startYear
                            true
                        }else if(todayYear>year && year==startYear){
                            //todayYear == startYear
                            dayOfYear>=startDayOfYear
                        }else if(todayYear==year && year>startYear){
                            //todayYear == startYear
                            dayOfYear<=todayDayOfYear
                        }else {
                            false
                        }
                        )

                fun callClick(){
                    val data = onDayClick.invoke(((dayOfYear) to (year))
                            to ((dayOfMonth) to (valueIsCheckDay.value?: isCheckDay))).apply {}

                    data?.let{
                        valueIsMissDay.value = data.isMissedDay
                        valueIsCheckDay.value = data.isCheckedDay
                        valueIsHighlightedDay.value = data.isHighlightedDay
                    }

                }
                Box(




                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(onClick = {
                            if(!block || (valueIsHighlightedDay.value?: isHighlightedDay)){

                                if ((valueIsHighlightedDay.value?: isHighlightedDay)) {
                                    showPopupChangeVoid.value = {if(!block) callClick()}
                                    showPopupText.value =  originalHabitCalendarValues?.highlightedExplain
                                    showPopup.value = true
                                } else {

                                    callClick()
                                }
                            }else{
                                if(beforeDate){
                                    onBeforeStartDayClick.invoke(dayOfYear to year)
                                }else{
                                    if(year>todayYear || ((todayYear== year) && dayOfYear>todayDayOfYear)){
                                        onAfterStartDayClick.invoke(dayOfYear to year)
                                    }else{
                                        if(!todayHabitDay){

                                        }
                                    }
                                }
                            }
                        })
                        .aspectRatio(1f)
                        .background(
                            (if ((valueIsHighlightedDay.value?: isHighlightedDay)) highlightedDayColor else if ((valueIsMissDay.value?: isMissDay))
                                missedDaysColor else if ((valueIsCheckDay.value?: isCheckDay)) checkDaysColor
                            else defaultColor).let{
                                if(block && !isHighlightedDay){
                                    if(!todayHabitDay || beforeDate) Color.Black.copy(0.2f) else defaultColor//Color.Black.copy(0.05f)
                                }else it
                            }
                        )
                        .border(
                            if ((todayDayOfYear == dayOfYear && year == todayYear) || startDayOfYear == dayOfYear && year == startYear) {
                                1.dp
                            } else if(isHighlightedDay && (isMissDay || isCheckDay)) 1.dp
                            else 0.dp, if (todayDayOfYear == dayOfYear && year == todayYear
                                || startDayOfYear == dayOfYear && year == startYear) {
                                MaterialTheme.colorScheme.onSecondary
                            } else if(isHighlightedDay && (isMissDay || isCheckDay))
                            {
                                if(isCheckDay) checkDaysColor else missedDaysColor
                            }
                                else Color.Transparent
                        )
                        .padding(10.dp)
                        // .align(Alignment.CenterHorizontally)

                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {


                    if((valueIsHighlightedDay.value?: isHighlightedDay)){
                        Box(Modifier.fillMaxSize()) {

                            highlightedDayIcon?.let{highlightedDayIcon->

                                Image(
                                    highlightedDayIcon,
                                    contentDescription = "")
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                            ) {
                                Text(text = "", fontSize = 5.sp, style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold), textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.BottomEnd)) }




                        }

                    }else{
                        Text(
                            text = "${ dayOfMonth}",
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



    val scrollState = rememberScrollState()
    Row(modifier.horizontalScroll(scrollState)){



        Row(modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp)) {
            Image(Icons.Default.NavigateBefore, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
                , contentDescription = "", modifier = Modifier
                    .fillMaxHeight()

                    .clickable {
                        previousBtn.invoke()
                    }
            )

            Text("    İlk Güne \n    Git", textAlign = TextAlign.Center ,color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    goToHabitStartDayBtn.invoke()
                })


        }



        Spacer(Modifier.width(8.dp))

        Row(modifier,

            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {




            Row (Modifier.height(240.dp).width(1.dp).background(Color.White.copy(0.6f))){
                Text("")
            }

            Spacer(Modifier.width(8.dp))
            for(i in monthIndexesAndYear){

                val monthIndex = i.first
                val year = i.second


                val currentCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH,monthIndex)
                    set(Calendar.DAY_OF_MONTH,1)
                }


                val dayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR)
                val dayOfWeek = (currentCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

                val days = getDaysOfMonth(currentCalendar.clone() as Calendar)





                Spacer(Modifier.width(4.dp))
                Column(Modifier
                    .width(300.dp)
                    .height(340.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if(showMonthLabels){
                        val ayIsmi = monthNames[currentCalendar.get(Calendar.MONTH)]
                        val year = currentCalendar.get(Calendar.YEAR)
                        Text(ayIsmi + (year.takeIf { it!=todayYear }?.let{
                            " - $it"
                        }?:""),modifier
                            .fillMaxWidth()
                            .padding(16.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSecondary)
                        Spacer(Modifier.height(4.dp))
                    }


                    getDay(days, dayOfYears = List(days.size){ dayOfYear+it }, years =List(days.size){year}
                    ,dayIndexesOfWeek=List(days.size){ (dayOfWeek + it) % 7 }, position = dayOfWeek)


                }

                Spacer(Modifier.width(8.dp))

                Row (Modifier.height(240.dp).width(1.dp).background(Color.White.copy(0.6f))){
                    Text("")
                }

                Spacer(Modifier.width(8.dp))
            }

            LaunchedEffect(monthIndexesAndYear) {


                loadCheckValues?.invoke(currentDayOfYearCheckValues.value)?.let{

                    checkedDays.value = it

                }


            }





        }


        Spacer(Modifier.width(8.dp))



        Row(modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp)) {


            Text("Bugüne     \nGit    ", textAlign = TextAlign.Center ,color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    goToToDayBtn.invoke()
                })

            Image(Icons.Default.NavigateNext, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
                , contentDescription = "", modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        nextBtn.invoke()

                    })

        }






    }

    LaunchedEffect(Unit) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

}


data class habitCalendarValues(
    val dayOfYear: Int,
    val year: Int,
    var dayOfMonth: Int?=null,

    var isMissedDay: Boolean,
    var isCheckedDay: Boolean,
    var isHighlightedDay: Boolean,

    val checkDayColor: Color=Color.Green.copy(0.7f),
    val missedDayColor: Color=Color.Red.copy(0.4f),
    var highlightedDayColor: Color = Color.Yellow.copy(0.5f),
    var highlightedDayIcon: ImageVector = Icons.Default.MilitaryTech,
    var highlightedExplain: String? = null,
    )


@Composable
fun ChatBubblePopup(show: Boolean, changeVoid: ()->Unit, onDismiss: () -> Unit, message: String, optionText: String = "\n\nİşaretle"
,color: Color = MaterialTheme.colorScheme.secondary) {
    if (show) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismiss
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color
                    )
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color.copy(0.5f),
                        modifier = Modifier.clickable {
                            changeVoid.invoke()
                        }
                    )
                }
            }
        }
    }
}



