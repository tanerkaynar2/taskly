package com.taner.taskly.presentation.ui.components


import androidx.core.graphics.ColorUtils
import android.graphics.drawable.shapes.OvalShape
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.taner.taskly.core.utils.DateUtils
import java.util.Calendar
import kotlin.random.Random

@Composable
fun CompletionGraph(
    completionData: List<Pair<String, Int>>, // Zaman, Tamamlanma Oranı
    completationDataForStreakAnalyzer:HashMap<Pair<Int, Int>,Boolean?>, // seri analizler için dayofyear year
    timePeriod: String, // Günlük, Haftalık, Aylık
    onTimePeriodChange: (String) -> Unit, // Time Period için Spinner tıklama fonksiyonu
    onChangeMonthRangeChange: (Int) -> Unit, // Time Period için Spinner tıklama fonksiyonu
    selectedOptionMonthCount: (Int), // Time Period için Spinner tıklama fonksiyonu
    color: Color,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        // Spinner

        var expanded by remember { mutableStateOf(false) }

        var bgColor by remember { mutableStateOf<Brush?>(null) }
        var bgPadding by remember { mutableStateOf(0.dp) }

        val context = LocalContext.current

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = bgPadding, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

            Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                completionData.lastOrNull()?.let {
                    val percentage = it.second
                    val gradientColors = when {
                        percentage < 40 -> listOf(Color(0xFFD32F2F), Color(0xFFFF5252)) // Kırmızı
                        percentage < 70 -> listOf(Color(0xFFFBC02D), Color(0xFFFFEB3B)) // Sarı
                        else -> listOf(Color(0xFF388E3C), Color(0xFF81C784)) // Yeşil
                    }

                    24.dp.let{if(bgPadding != it )bgPadding = it}
                    if(bgColor==null)bgColor = Brush.horizontalGradient(gradientColors)

                    Image(Icons.Default.Info,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                        modifier =  Modifier
                            .size(16.dp)
                            .clickable {
                                Toast.makeText(context, "Barlara tıklayarak o zamana ait ilerleme detaylarını görebilirsin.", Toast.LENGTH_SHORT).show()
                            })

                    Box(
                        modifier = Modifier
                            // .padding(12.dp)
                            // .clip(RoundedCornerShape(16.dp))
                            //    .background()
                            .weight(3f)
                        // .shadow(6.dp, RoundedCornerShape(16.dp)),
                        , contentAlignment = Alignment.Center
                    ) {

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                        , horizontalArrangement = Arrangement.Center){
                            Text(
                                text = "                Puan: ",
                                color = gradientColors.randomOrNull()?: MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier//.padding(8.dp)
                            )
                            PercentageCircle(percentage,gradientColors.randomOrNull()?: color)

                        }


                    }
                }



                Box(Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(.7f))
                    .clickable { expanded = !expanded }
                    , contentAlignment = Alignment.Center){

                    Row(verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = timePeriod,
                            modifier = Modifier
                                .padding(4.dp)
                            ,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(Icons.Default.ArrowDropDown,"", tint = MaterialTheme.colorScheme.onSecondary)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        CompletionGraphOptions
                            .forEachIndexed { index, item ->
                                DropdownMenuItem(
                                    text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                                    onClick = {

                                        onTimePeriodChange.invoke(item)

                                        expanded = false
                                    }
                                )
                            }
                    }
                }

            }

        }



        Spacer(modifier = Modifier.height(16.dp))

        // Grafik (Line veya Bar Chart)



        var expanded2 by remember { mutableStateOf(false) }
        val monthCounts = listOf(1,3,6,9,12,24)
        val optionNames = listOf("Son 1 ay","Son 3 ay","Son 6 ay","Son 9 ay","Son 1 yıl","Son 2 yıl")

        Row(Modifier, horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Text("Grafik: $timePeriod     ", color = MaterialTheme.colorScheme.onSecondary)


            DropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {


                optionNames.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                            onClick = {

                                onChangeMonthRangeChange.invoke(monthCounts[index])

                                expanded2 = false
                            }
                        )
                    }
            }

            Image(Icons.Default.UnfoldMore,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
            , modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        expanded2 = true
                    })
            Text("   ${optionNames.get(monthCounts.indexOf(selectedOptionMonthCount))}", color = MaterialTheme.colorScheme.onSecondary)
        }


        Spacer(modifier = Modifier.height(8.dp))


        var showPopupText = remember { mutableStateOf<String?>(null) }
        var showPopup = remember { mutableStateOf<String?>(null) }
        var showPopupVoid = remember { mutableStateOf<()->Unit>({}) }

        val ratio = 2
        val listState = rememberLazyListState()
        LaunchedEffect(completionData.size) {
            listState.scrollToItem(index = (listState.layoutInfo.totalItemsCount - 1).takeIf { it>-1 }?:0)
        }

        LazyRow(Modifier.height((140 * ratio).dp), verticalAlignment = Alignment.Bottom,
            state = listState) {
            items(completionData) { item ->

                Spacer(modifier = Modifier.width(8.dp))

                Column(Modifier.clickable {
                    showPopupText.value = item.first.split("-|-").takeIf { it.size==2 }?.let{
                        it.firstOrNull()?.let{
                            it
                        }?: item.first.substringAfterLast("|")
                    }?: item.first.substringAfterLast("|")
                    showPopup.value = item.first.substringAfterLast("|")
                    showPopupVoid.value = {
                        showPopup.value = null

                    }
                }) {

                    val height = item.second * ratio


                    if(item.first.contains("0|") || item.second>0){
                        Text("${item.second}%\n",color =
                            if(item.first.contains("1|")) Color.Green else if(item.first.contains("0|")) Color.Red
                            else MaterialTheme.colorScheme.onSecondary)

                    }

                    Box(Modifier
                        .background(color)
                        .height(height.dp), contentAlignment = Alignment.TopCenter){

                        Box(Modifier
                            .fillMaxHeight()
                            .width(12.dp))

                        if(showPopup.value==item.first.substringAfterLast("|")){

                            showPopupText.value?.let {
                                ChatBubblePopup(showPopup.value!=null,{
                                    showPopupVoid.value.invoke()
                                },{
                                    showPopup.value = null
                                }, it,"\n\nTAMAM")//,color)
                            }
                        }
                    }
                    Text("\n${item.first.substringAfterLast("|").split(" ").let{
                        it.firstOrNull() + (it.getOrNull(1)?.let { 
                            "\n${it.take(3)}"
                        }?:"")
                    }}",color =
                        if(item.first.contains("1|")) Color.Green else if(item.first.contains("0|")) Color.Red
                    else MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Center)




                }

                Spacer(modifier = Modifier.width(8.dp))

            }
        }



        data class StreakData(
            var dayOfYear: Int,
            var year: Int,
            var count: Int,
        )
        val streakList = mutableListOf<StreakData>()
        val streakColorList = remember { mutableStateOf(mutableListOf<Color>()) }
        var beforeCheck : Boolean?=null
        var i = 0

        val sortedList = completationDataForStreakAnalyzer.keys.toList()
            .sortedWith(compareBy<Pair<Int, Int>> { it.second }  // year
                .thenBy { it.first })                            // dayOfYear

        sortedList.forEach {t->
            val u = completationDataForStreakAnalyzer[t]
            if(i == 0 ){
                streakList.add(StreakData(
                    dayOfYear = t.first,
                    year = t.second,
                    count = if(u==true) 1 else 0
                ))
                i++
            }else{

                if(u==true){
                    if(beforeCheck==true){
                        streakList.lastOrNull()?.let{
                            var newDayOfYear = it.dayOfYear
                            var newYear = it.year
                            if(t.second == newYear && t.first<newDayOfYear){
                                newDayOfYear = t.first
                            }else if(t.second<newYear){
                                newYear = t.second
                                newDayOfYear = t.first
                            }
                            streakList[streakList.lastIndex] = it.copy(count=it.count+1,
                                dayOfYear = newDayOfYear, year = newYear)
                        }
                    }else{
                        streakList.add(StreakData(
                            dayOfYear = t.first,
                            year = t.second,
                            count = 1
                        ))
                    }
                }else if(u ==false){

                }else{

                }
            }
            beforeCheck = u
        }




        if(streakList.any { it.count>2 }){
            Text("\n\n\n\n\n\n${optionNames.get(monthCounts.indexOf(selectedOptionMonthCount))} En iyi Seriler\n\n", modifier = Modifier
                .fillMaxWidth().clickable {
                    expanded2 = true
                },
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSecondary)

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                val myCalendar = Calendar.getInstance().apply {
                }

                streakList/*.sortedWith(compareBy<StreakData> { it.year }.thenBy { it.dayOfYear })*/.forEachIndexed { i, streakData ->

                    if(streakData.count>2){

                        val sColor = streakColorList.value.getOrNull(i)?: MaterialTheme.colorScheme.onSecondary

                        myCalendar.set(Calendar.DAY_OF_YEAR,streakData.dayOfYear)
                        myCalendar.set(Calendar.YEAR,streakData.year)


                        Row(Modifier, horizontalArrangement = Arrangement.Center
                        , verticalAlignment = Alignment.CenterVertically){

                            Box(Modifier
                                .height(12.dp)
                                .width((streakData.count.coerceAtMost(60) * ratio * 3).dp)
                                , contentAlignment = Alignment.Center){

                                Box(Modifier
                                    .fillMaxSize()
                                    .background(sColor), contentAlignment = Alignment.Center){

                                    Text(streakData.count.toString(),
                                        fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondary)

                                }
                            }

                            Text("    ${myCalendar.get(Calendar.DAY_OF_MONTH)} " +
                                    "${DateUtils.monthNames[myCalendar.get(Calendar.MONTH)]}" ,
                                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondary)

                        }


                        Spacer(Modifier.height(12.dp))


                    }

                }



            }
        }


        LaunchedEffect(streakList) {
            fun generateRandomClosedToneGradient(streaks: List<Int>): List<Color> {
                if (streaks.isEmpty()) return emptyList()

                val min = streaks.minOrNull()!!
                val max = streaks.maxOrNull()!!
                val range = (max - min).takeIf { it != 0 } ?: 1

                // Rastgele ama uyumlu bir palet için temel hue seç
                val baseHue = Random.nextFloat() * 360f  // 0 - 360
                val hueRange = Random.nextFloat() * 60f + 20f  // 20 - 80 arası aralık (çok dar olmasın ama geniş de değil)
                val reverse = Random.nextBoolean()  // Aralığı ileri mi terse mi gitmeli (renk farkı)

                val saturationBase = 0.2f + Random.nextFloat() * 0.3f // 0.2 - 0.5 (doygunluk düşük)
                val brightnessBase = 0.3f + Random.nextFloat() * 0.3f // 0.3 - 0.6 (koyu renkler için)

                return streaks.map { value ->
                    val ratio = (value - min).toFloat() / range
                    val hue = if (reverse)
                        (baseHue - ratio * hueRange + 360f) % 360f
                    else
                        (baseHue + ratio * hueRange) % 360f

                    val brightness = brightnessBase + ratio * 0.2f // azıcık artan parlaklık
                    val saturation = saturationBase + ratio * 0.1f // azıcık artan doygunluk

                    Color.hsv(hue, saturation.coerceIn(0f, 1f), brightness.coerceIn(0f, 1f))
                }
            }
            val colors = generateRandomClosedToneGradient(streakList.map { it.count })
            streakColorList.value = colors.toMutableList()



        }



    }
}

@Composable
fun PercentageCircle(percentage: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = 1f,
            color = color.copy(0.4f),
            strokeWidth = 6.dp
        )

        CircularProgressIndicator(
            progress = (percentage.coerceIn(0, 100) / 100f),
            color = color,
            strokeWidth = 2.dp
        )

        // Yüzde yazısı
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}


var CompletionGraphOptions = listOf("Günlük", "Haftalık", "Aylık")