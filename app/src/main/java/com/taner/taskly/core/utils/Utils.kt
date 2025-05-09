package com.taner.taskly.core.utils

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class Utils {
    companion object{


        fun findClosestToFiveAboveElseBelow(number: Int , list: List<Int>): Int? {
            val greater = list.filter { it > number }.minOrNull()
            return greater ?: list.filter { it < number }.maxOrNull()
        }



    }
}

fun <T> MutableList<T>.swap(i: Int, j: Int)  {
    val temp = this[i]
    this[i] = this[j]
    this[j] = temp
}
