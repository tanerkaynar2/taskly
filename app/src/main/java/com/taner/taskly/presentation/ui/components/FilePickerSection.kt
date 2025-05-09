package com.taner.taskly.presentation.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.rememberPermissionState
import com.taner.taskly.MainActivity.Companion.REQUEST_CODE_READ_STORAGE
import com.taner.taskly.MainActivity.Companion.grantedCallback
import com.taner.taskly.MainActivity.Companion.isLightTheme


@Composable
fun FilePickerSection(
    selectedFiles: List<Uri>,
    onFilesSelected: (List<Uri>) -> Unit,
    onFilesRemove: (List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        onFilesSelected(uris)
    }

    Row {


        Box(Modifier
            .height(100.dp)
            .weight(5f)
            .align(Alignment.CenterVertically)
            .padding(8.dp)){
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items(selectedFiles.size) { index ->

                    if(index==0){
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clickable {
                                expanded = true
                            },contentAlignment = Alignment.Center
                        ){

                            Image(
                                Icons.Default.ArrowDropDown,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.CenterEnd)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                selectedFiles.forEachIndexed { index, item ->

                                    DropdownMenuItem(
                                        text = {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ){
                                                Icon(
                                                    imageVector =  getFileIcon(item),
                                                    contentDescription = "",
                                                    tint = MaterialTheme.colorScheme.onSecondary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))


                                                Text(
                                                    text = getFileNameFromUri(context,item)?: item.path?.substringAfterLast("/") ?: "Dosya $index",
                                                     color = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))
                                                IconButton(onClick = {

                                                    onFilesRemove(listOf(item))

                                                  //  expanded = false
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "",
                                                        tint = MaterialTheme.colorScheme.onSecondary
                                                    )



                                                }
                                            }
                                        },
                                        onClick = {

                                            val uri = item
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, context.contentResolver.getType(uri))
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }
                                            context.startActivity(intent)

                                            expanded = false
                                        }
                                    )
                                }
                            }

                        }
                    }

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .sizeIn(maxWidth = 150.dp, maxHeight = 100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else Color.DarkGray)
                            .clickable {

                                val file = selectedFiles[index]

                                val uri = file
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, context.contentResolver.getType(uri))
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(intent)


                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val file = selectedFiles[index]

                        /*Image(
                            getFileIcon(file),null, colorFilter = ColorFilter.tint(Color.Red),

                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp)
                                .align(Alignment.CenterStart)
                                .padding(4.dp, 0.dp, 0.dp, 0.dp)
                        )*/


                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 30.dp).sizeIn(maxHeight = 80.dp)) {
                            Spacer(modifier = Modifier.width(16.dp))
                            UniversalFilePreview(file)




                            Spacer(modifier = Modifier.width(16.dp))


                        }

                        Text(
                            text = getFileNameFromUri(context,file) ?: "Dosya",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondary,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold, modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 30.dp, 0.dp, 10.dp).align(Alignment.BottomCenter)
                        )

                        Image(
                            Icons.Default.Delete,null, colorFilter = ColorFilter.tint(Color.Red),

                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp)
                                .align(Alignment.CenterEnd)
                                .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                .clickable {

                                    onFilesRemove(listOf(file))
                                }
                        )
                        }

                }

            }
        }








        Button(onClick = {



            launcher.launch(arrayOf(
                "application/pdf",
                "image/*",
                "video/*",
                "audio/*",
                "text/plain",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))



        }, modifier = Modifier.height(36.dp).weight(1.5f).align(Alignment.CenterVertically)) {

            Text("Ekle", color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp, modifier = Modifier.weight(2f))

            Spacer(modifier = Modifier.height(2.dp))

            Image(Icons.Default.AttachFile,"",colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary), modifier = Modifier.weight(1f))

        }




    }
}





fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf("_data")
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow("_data")
        if (it.moveToFirst()) {
            return it.getString(columnIndex)
        }
    }
    return null
}


@Composable
fun getFileIcon(uri: Uri): ImageVector {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri)
    val path = uri.path.toString()

    return when {
        mimeType?.startsWith("video/") == true -> Icons.Default.PlayCircle
        mimeType?.startsWith("audio/") == true -> Icons.Default.AudioFile
        mimeType == "application/pdf" -> Icons.Default.PictureAsPdf
        mimeType?.contains("word") == true -> Icons.Default.Description
        mimeType?.contains("text") == true -> Icons.Default.TextSnippet
        path.endsWith(".pdf") -> Icons.Default.PictureAsPdf
        path.endsWith(".doc") || path.endsWith(".docx") -> Icons.Default.Description
        path.endsWith(".xls") || path.endsWith(".xlsx") -> Icons.Default.TableChart
        path.endsWith(".ppt") || path.endsWith(".pptx") -> Icons.Default.Slideshow
        path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") -> Icons.Default.Image
        path.endsWith(".mp3") || path.endsWith(".wav") -> Icons.Default.Audiotrack
        path.endsWith(".mp4") || path.endsWith(".avi") -> Icons.Default.Movie
        else -> Icons.Default.InsertDriveFile
    }
}





fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver

        val returnCursor = contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                it.getString(nameIndex)
            } else null
        }

    }
    catch (e: Exception) {
        null
    }
}
