package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taner.taskly.MainActivity
import com.taner.taskly.domain.model.Habit


@Composable
fun LongestStreaksCard(streaks: List<Habit>, modifier: Modifier = Modifier, onClick: (Habit)->Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (MainActivity.isDarkTheme()) Color.DarkGray else MaterialTheme.colorScheme.onSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔁 En Uzun Seriler (Streaks)",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))


            streaks.forEach {


                Row(modifier = Modifier.clickable {
                    onClick.invoke(it)
                }){
                    Text(
                        text = "• ${it.name}: ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                    )

                    Text(
                        text = "${it.longestStreak}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if(it.longestStreak == it.currentStreak && it.currentStreak>1) Color(0xFF00FF0A) else MaterialTheme.colorScheme.onSecondary
                        )

                    )

                    Text(
                        text = " gün",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                    )

                }



            }
        }
    }
}
