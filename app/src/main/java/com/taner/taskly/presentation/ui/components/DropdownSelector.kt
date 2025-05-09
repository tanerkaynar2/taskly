package com.taner.taskly.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropdownSelector(
    items: List<T>,
    selectedItem: T,
    onItemDelClicked: ((T) -> Unit)?=null,
    hideDelButtonIndexes: (List<Int>)?=null,
    onItemSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf(items) }
    var selectedItemText by remember { mutableStateOf(selectedItem) }


    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedItemText.toString(), color = MaterialTheme.colorScheme.onSecondary)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {

                        if(onItemDelClicked==null || hideDelButtonIndexes?.contains(index) == true) Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary)
                           else{

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ){

                                Text(
                                    text =item.toString(),
                                    color = MaterialTheme.colorScheme.onSecondary
                                            ,modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                IconButton(onClick = {
                                    items = items - item
                                   onItemDelClicked.invoke(item)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }

                           },
                    onClick = {
                        onItemSelected(item)
                        selectedItemText = item
                        expanded = false
                    }
                )
            }
        }
    }
}
