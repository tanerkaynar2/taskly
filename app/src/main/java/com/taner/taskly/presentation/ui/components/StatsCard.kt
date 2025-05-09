package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.domain.model.Task

@Composable
fun StatsCard(
    tasks: List<Task>,
    selectedDate: Long,
    actionClick: ()->Unit,
    click: ()->Unit,
) {
    val todayTasks = tasks.filter { it.date == selectedDate || it.repetition != null }
    val completed = todayTasks.count { it.isCompleted }
    val total = todayTasks.size
    val percentage = if (total == 0) 0 else (completed * 100 / total)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp).clickable { click.invoke() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("İstatistikler", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.surfaceTint)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("Toplam Görev: $total", color = MaterialTheme.colorScheme.onSecondary)


                Text("Tamamlanan: $completed", color = MaterialTheme.colorScheme.onSecondary)
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth()
                , color = MaterialTheme.colorScheme.onSecondary
            )

            Text(
                "$percentage% tamamlandı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}
