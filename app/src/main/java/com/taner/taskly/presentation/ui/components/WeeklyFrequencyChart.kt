package com.taner.taskly.presentation.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taner.taskly.MainActivity.Companion.isLightTheme


@Composable
fun FrequencyChartCircleStyle(
    data: List<Pair<Pair<Int, Int>, List<Int>>>
    , onDayOfYearOffsetChange: (Int)->Unit
) {
    val scrollState = rememberScrollState()

    val sortedData = data.sortedBy { it.first.second * 100 + it.first.first }
    val dayLabels = listOf("Pzt", "Salı", "Çar", "Per", "Cum", "Cmt", "Paz")
    val color = if (isLightTheme()) Color.Black else Color.White

    val circleMinSize = 0.dp
    val circleStep = 4.dp
    val maxSize = 24.dp
    val rowHeight = 34.dp


    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Sıklık",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )

        var optionNames = listOf("Son 1 hafta", "Son 2 hafta", "Son 1 ay","Son 2 ay", "Son 3 ay","Son 6 ay", "Son 1 yıl")
        var dayCounts = listOf(7,14,30,60,90,180,365)
        var expanded2 by remember { mutableStateOf(false) }
        var selectedOptionMonthCount by remember { mutableStateOf(90) }

        DropdownMenu(
            expanded = expanded2,
            onDismissRequest = { expanded2 = false }
        ) {

            optionNames.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                    onClick = {

                        onDayOfYearOffsetChange.invoke(dayCounts[index])
                        selectedOptionMonthCount = dayCounts[index]
                        expanded2 = false
                    }
                )
            }
        }

        Image(
            Icons.Default.UnfoldMore,"", colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
            , modifier = Modifier
                .size(16.dp)
                .clickable {
                    expanded2 = true
                })
        Text("   ${optionNames.get(dayCounts.indexOf(selectedOptionMonthCount))}", color = MaterialTheme.colorScheme.onSecondary)
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(start = 8.dp, end = 8.dp)
    ) {
        // Y ekseni - gün isimleri ve çizgiler
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            dayLabels.forEach { day ->
                Box(
                    modifier = Modifier
                        .height(rowHeight)
                        .width(40.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = day, fontSize = 12.sp, color = color)
                }
            }
        }

        // X ekseni - aylar ve daireler
        sortedData.reversed().forEach { (monthYear, dayCounts) ->
            val (month, year) = monthYear

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                dayLabels.forEachIndexed { index, _ ->
                    val count = dayCounts.getOrNull(index) ?: 0
                    val size = (circleMinSize + circleStep * count).coerceAtMost(maxSize)
                    val circleColor = when (count) {
                        0 -> Color.Transparent
                        1 -> Color(0xFFD32F2F) // kırmızı
                        2 -> Color(0xFFECECEC) // beyaz ton
                        3 -> Color(0xFF525252) // koyu beyaz (gri ton)
                        else -> Color(0xFF388E3C) // yeşil
                    }


                    Box(
                        modifier = Modifier
                            .height(rowHeight)
                            .width(40.dp), // sabit hücre
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(size)) {
                            drawCircle(color = circleColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${monthName(month)} $year",
                    fontSize = 12.sp,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    /*// En sağa scroll
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }*/
}


fun monthName(month: Int): String = when (month) {
    1 -> "Oca"
    2 -> "Şub"
    3 -> "Mar"
    4 -> "Nis"
    5 -> "May"
    6 -> "Haz"
    7 -> "Tem"
    8 -> "Ağu"
    9 -> "Eyl"
    10 -> "Eki"
    11 -> "Kas"
    12 -> "Ara"
    else -> "?"
}
