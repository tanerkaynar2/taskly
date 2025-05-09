package com.taner.taskly.presentation.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun UniversalFilePreview(uri: Uri, size: Dp = 120.dp, iconSize: Dp = 64.dp) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val mimeType = remember(uri) { contentResolver.getType(uri) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                imageBitmap = bitmap.asImageBitmap()
                failed = false
            } else {
                failed = true
            }
        } catch (e: Exception) {
            failed = true
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Preview",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else if (failed) {
        // Render fallback icon based on MIME type
        val icon = when {
            mimeType?.startsWith("video/") == true -> Icons.Default.PlayCircle
            mimeType?.startsWith("audio/") == true -> Icons.Default.AudioFile
            mimeType == "application/pdf" -> Icons.Default.PictureAsPdf
            mimeType?.contains("word") == true -> Icons.Default.Description
            mimeType?.contains("text") == true -> Icons.Default.TextSnippet
            else -> Icons.Default.InsertDriveFile
        }

        Icon(
            imageVector = icon,
            contentDescription = "Dosya",
            modifier = Modifier.size(iconSize),
            tint = Color.Gray
        )
    } else {
        Image(
            Icons.Default.InsertDriveFile,
            contentDescription = "Preview",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
