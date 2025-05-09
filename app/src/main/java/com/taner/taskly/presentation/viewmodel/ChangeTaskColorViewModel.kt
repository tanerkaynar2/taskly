package com.taner.taskly.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


class ChangeTaskColorViewModel : ViewModel() {
    val shouldRefresh = mutableStateOf(false)

    fun triggerRefresh() {
        shouldRefresh.value = true
    }

    fun refreshDone() {
        shouldRefresh.value = false
    }





    val shouldRefreshForHabit = mutableStateOf(false)

    fun triggerRefreshForHabit() {
        shouldRefreshForHabit.value = true
    }

    fun refreshDoneForHabit() {
        shouldRefreshForHabit.value = false
    }





    val shouldRefreshForHabitEditDialog = mutableStateOf(false)
    fun triggerRefreshForHabitEditDialog() {
        shouldRefreshForHabitEditDialog.value = true
    }
    fun refreshDoneForHabitEditDialog() {
        shouldRefreshForHabitEditDialog.value = false
    }


    val shouldRefreshForHabitDelDialog = mutableStateOf(false)
    fun triggerRefreshForHabitDelDialog() {
        shouldRefreshForHabitDelDialog.value = true
    }
    fun refreshDoneForHabitDelDialog() {
        shouldRefreshForHabitDelDialog.value = false
    }



}