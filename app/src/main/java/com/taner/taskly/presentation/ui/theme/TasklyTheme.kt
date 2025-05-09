package com.taner.taskly.presentation.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight


// Light ve Dark Theme renklerini tanımlıyoruz.
private val LightColorPalette = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    secondary = Color(0xFF4B2492),
    onSecondary = Color.Black,
    background = Color(0xFFE0E0E0),
    onBackground = Color.Black,
    surface = Color.White,
    surfaceTint = Color.DarkGray,
    surfaceContainer = Color.White,
    onSurfaceVariant = Color(0xFF5F5F5F),
    tertiaryContainer = Color(0xFF3B3B3B),
    inverseOnSurface = Color.White,
    onSurface = Color.Black
)

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF565656),
    onPrimary = Color.Black,
    secondary = Color(0xFF4B2492),
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    tertiaryContainer = Color(0xFF959595),
    onSurface = Color.White,
)

// Uygulamanın genel tema yapısını belirliyoruz
@Composable
fun TasklyTheme(
    themeMode: Int = 0, // Kullanıcının tercihine göre tema seçimi
    content: @Composable () -> Unit
) {
    val colors = if (themeMode == 0) {
        DarkColorPalette
    } else {
        LightColorPalette
    }



    MaterialTheme(
        colorScheme = colors,
        typography = typography, // Yazı tipi
        content = content
    )
}


// Özel yazı tipi ve tema özellikleri
val typography = Typography(
    displayLarge = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    ),
    displayMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Gray
    ),
    displaySmall = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        color = Color.LightGray
    ),
    headlineLarge = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    ),
    titleSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color.LightGray
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        color = Color.LightGray
    )
)

@Composable
fun TasklyThemePreview() {
    TasklyTheme {
        // Bu preview'da test edilecek içerik
        Text("Merhaba, Taskly!")
    }
}
