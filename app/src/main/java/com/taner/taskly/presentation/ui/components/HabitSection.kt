package com.taner.taskly.presentation.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.domain.model.CountsResult
import com.taner.taskly.domain.model.Habit
import com.taner.taskly.presentation.ui.screen.habits.StylishButton
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.isInitHabitSection


@Composable
fun HabitSection(
    habits: List<Habit> = emptyList(),
    clickHabit: (Int) -> Unit,
    editHabit: (Int) -> Unit,
    delHabit: (Int) -> Unit,
    onHabitToggle: (Habit) -> Unit,
    activeToggle: (Int) -> Unit,
    totalCount: (Int) ,
    pageSize: (Int) ,
    totalProgress: CountsResult,
    thisIsChecked: (Habit)->Boolean,
    isSelectedNotCompleted: Boolean = false,
    nextPage: ()->Unit,
    loadMore: ()->Unit,
    beforePage: ()->Unit,
    filter: (String?)->Unit
) {

    var showLongClickDialog by remember { mutableStateOf(-1) }
    var showLongClickDialogHabitName by remember { mutableStateOf("") }
    var showDelDialog by remember { mutableStateOf(-1) }
    var isSelectedNotCompleted by remember { mutableStateOf(isSelectedNotCompleted) }

    if(showDelDialog!=-1){
        AlertDialog(
            onDismissRequest = { showDelDialog = -1 },
            title = {
                Text(text = "Emin misin?",color = MaterialTheme.colorScheme.onSecondary)
            },
            text = {
                Text("Bu öğeyi silmek istediğine emin misin?",color = MaterialTheme.colorScheme.onSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    delHabit.invoke(showDelDialog)



                    showDelDialog = -1
                }) {
                    Text("Evet",color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelDialog = -1 }) {
                    Text("İptal",color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }
    if(showLongClickDialog!=-1){
        AlertDialog(
            onDismissRequest = { showLongClickDialog = -1 },
            title = { Text(showLongClickDialogHabitName,color = MaterialTheme.colorScheme.onSecondary) },
            text = { Text("",color = MaterialTheme.colorScheme.onSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDelDialog = showLongClickDialog

                    showLongClickDialog = -1
                }) {
                    Text("Sil",color = Color.Red)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        activeToggle.invoke(showLongClickDialog)
                        showLongClickDialog = -1
                    }) {
                        Text( "Pasif   ",color = MaterialTheme.colorScheme.onSecondary)
                    }

                    TextButton(onClick = {
                        editHabit.invoke(showLongClickDialog)
                        showLongClickDialog = -1
                    }) {
                        Text("Düzenle",color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        )
    }







    Column(modifier = Modifier.padding(vertical = 16.dp)) {


        Row(Modifier.fillMaxWidth()
            , verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

            Text(text = "Alışkanlıklar ${(totalProgress.totalCount-totalProgress.completedCount).let { 
                if(it>0) "  ($it yapılmadı)" else ""
            }}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondary)


            if(totalCount>=pageSize && false){
                Spacer(Modifier.width(8.dp))
                Image(Icons.Default.NavigateBefore,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .width(24.dp)
                        .clickable {
                            beforePage.invoke()
                        }
                        .padding(horizontal = 4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Image(Icons.Default.NavigateNext,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .width(24.dp)
                        .clickable {
                            nextPage.invoke()
                        }
                        .padding(horizontal = 4.dp)
                )
            }

            Row(Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())){

                val activeColor: Color = Color.Gray.copy(0.7f)
                val inactiveColor: Color = Color.Gray.copy(0.2f)

                Spacer(Modifier.weight(1f))

                Box(Modifier
                    .width(70.dp)
                    .height(25.dp)
                    .clickable {

                    }){
                    TextButton(
                        onClick = {filter("all");isSelectedNotCompleted = false},
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                           // .background(if(!showIsNotCompleted) activeColor else inactiveColor)
                    ) {
                        Text(
                            text = "Tümü",
                            modifier = Modifier.fillMaxSize(),
                            color = if(isSelectedNotCompleted) MaterialTheme.colorScheme.onSecondary else Color.Cyan,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Box(Modifier
                    .width(70.dp)
                    .height(25.dp)
                    .clickable {

                    }){
                    TextButton(
                        onClick = {filter("show is not completed");isSelectedNotCompleted = true},
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                           // .background(if(!showIsNotCompleted) activeColor else inactiveColor)
                    ) {
                        Text(
                            text = "Yapılmamış",
                            modifier = Modifier.fillMaxSize(),
                            color = if(isSelectedNotCompleted) Color.Cyan else MaterialTheme.colorScheme.onSecondary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }





            }
        }


        Spacer(modifier = Modifier.height(8.dp))


        if(isInitHabitSection!=false){
            (habits).forEach { habit ->

                val isChecked = remember { mutableStateOf(thisIsChecked(habit)) }

                if((isSelectedNotCompleted && !isChecked.value) || (!isSelectedNotCompleted) ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        clickHabit.invoke(habit.id)
                                    },
                                    onLongPress = {


                                        showLongClickDialog = habit.id

                                        showLongClickDialogHabitName = habit.name
                                    }
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked =isChecked.value,
                            onCheckedChange = {
                                isChecked.value = !isChecked.value
                                onHabitToggle(habit)
                            }
                        )
                        Text(text = habit.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondary)
                    }
                }else{
                }





            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        if(totalCount>=pageSize|| !isInitHabitSection){
            Image(Icons.Default.KeyboardArrowDown,null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if(!isInitHabitSection){
                            isInitHabitSection = true
                            loadMore.invoke()
                        }else{
                            loadMore.invoke()
                        }
                    }
                    .padding(horizontal = 4.dp)
            )
        }

    }



}
