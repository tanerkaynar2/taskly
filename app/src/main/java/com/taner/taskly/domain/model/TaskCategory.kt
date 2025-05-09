package com.taner.taskly.domain.model

import android.content.SharedPreferences

enum class TaskCategory(val turkishName: String)  {
    DAILY("GÜNLÜK"),
    WEEKLY("HAFTALIK"),
    CUSTOM("EKLE");


    override fun toString(): String {
        return turkishName//super.toString()
    }

   companion object {
       fun getCategories(sp: SharedPreferences): Set<String>? {
           return sp.getStringSet("CATEGORIES",null)
       }

       fun saveCategories(sp: SharedPreferences, list: List<String>) {
           sp.edit().putStringSet("CATEGORIES",list.toSet()).apply()
       }

   }


}
