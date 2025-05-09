package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatItemWithProgress(
    title: String,
    percentage: Int,
    onClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    // Renk, yüzdelik orana göre ayarlanır
    val progressColor = when {
        percentage < 50 -> Color(0xFFFF0000) // kırmızı
        percentage < 80 -> Color(0xFFFFA000) // sarı
        percentage < 100 -> Color(0xFF00FF13) // yeşil
        else -> Color(0xFF00C207) // koyu yeşil (tamamlandı)
    }

    Column(
        modifier = modifier
            .clickable { onClick.invoke() }
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$title: %$percentage",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
        LinearProgressIndicator(
            progress = percentage / 100f,
            color = progressColor,
            trackColor = Color.LightGray,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}
