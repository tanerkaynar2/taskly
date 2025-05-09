package com.taner.taskly.presentation.ui.components

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taner.taskly.MainActivity.Companion.isLightTheme

@Composable
fun HourlyTrendChart(data: List<Pair<Float, Float>>,onDateChange: (Int)->Unit) {
    Column(modifier = Modifier.fillMaxSize()) {


        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\uD83D\uDD53 Ortalama Yapılma Saat Grafiği ",
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

                            onDateChange.invoke(dayCounts[index])
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

        // Grafik İçeriği
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Verileri normalize et
            val maxX = data.maxOf { it.first }
            val maxY = data.maxOf { it.second }

            // Eksen çizgileri (Yatay ve dikey)
            drawLine(
                color = Color.Gray,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(0f, height),
                end = Offset(0f, 0f),
                strokeWidth = 2f
            )

            // Path çizim
            val path = Path()
            data.forEachIndexed { index, pair ->
                val x = width * (pair.first / maxX)
                val y = height - (height * (pair.second / maxY))

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Çizgiyi çizin
            drawPath(path, color = Color.Blue, style = Stroke(width = 4.dp.toPx()))

            // Noktaları ve etiketlerini çiz
            data.forEachIndexed { index, pair ->
                val x = width * (pair.first / maxX)
                val y = height - (height * (pair.second / maxY))

                // Noktayı çiz
                drawCircle(Color.Red, radius = 6f, center = Offset(x, y))

                // Nokta etiketini ekle
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "(${pair.first.toInt()}:${pair.second.toInt()})",
                        x + 10, y - 10,
                        Paint().apply {
                            color = if(isLightTheme()) Color.Black.hashCode() else Color.White.hashCode()
                            textSize = 16f
                            isAntiAlias = true
                        }
                    )
                }
            }

            // X eksenindeki saat etiketlerini ekle (0-23 saat)
            for (i in 0..23) {
                val x = width * (i / maxX)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${i}",  // Saat formatında yazılıyor
                        x - 15, height + 20,
                        Paint().apply {
                            color = if(isLightTheme()) Color.Black.hashCode() else Color.White.hashCode()
                            textSize = 16f
                            isAntiAlias = true
                        }
                    )
                }
            }

            // Y eksenindeki etiketler (örnek olarak 1, 2, 3, vs.)
            for (i in 1..maxY.toInt()) {
                val y = height - (height * (i / maxY))
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "$i", // Y ekseni için veriler
                        10f, y + 5,
                        Paint().apply {
                            color = if(isLightTheme()) Color.Black.hashCode() else Color.White.hashCode()
                            textSize = 16f
                            isAntiAlias = true
                        }
                    )
                }
            }

            // X ekseni temsilinin ismi: "Saat"
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "Saat",
                    width / 2 - 20, height + 40,
                    Paint().apply {
                        color = if(isLightTheme()) Color.Black.hashCode() else Color.White.hashCode()
                        textSize = 18f
                        isAntiAlias = true
                    }
                )
            }

            // Y ekseni temsilinin ismi: "Dakika"
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "Dakika",
                    0f, height + 47,
                    Paint().apply {
                        color = if(isLightTheme()) Color.Black.hashCode() else Color.White.hashCode()
                        textSize = 18f
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

