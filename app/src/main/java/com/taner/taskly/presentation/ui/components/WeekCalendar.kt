package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun WeekCalendar(
    selectedDate: Long,
    selectFirst: Boolean = false,
    firstDate: ((Long) -> Unit)?=null,
    onDateSelected: (Long) -> Unit
) {
    val weekDates = remember {
        val calendar = Calendar.getInstance()
        val list = mutableListOf<Long>()
        for (i in 0..6) {
            val clone = calendar.clone() as Calendar
            clone.add(Calendar.DAY_OF_YEAR, i)
            clone.set(Calendar.HOUR_OF_DAY, 0)
            clone.set(Calendar.MINUTE, 0)
            clone.set(Calendar.SECOND, 0)
            clone.set(Calendar.MILLISECOND, 0)
            list.add(clone.timeInMillis)
        }
        list
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(weekDates) { dateMillis ->
            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val isSelected = selectedDate == dateMillis

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onDateSelected(dateMillis) }
                    .padding(8.dp)
            ) {
                Text(text = dayOfWeek, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
                Text(text = dayOfMonth, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }


    if(selectFirst){
        onDateSelected.invoke(weekDates.first())
    }
    firstDate?.invoke(weekDates.first())
}
