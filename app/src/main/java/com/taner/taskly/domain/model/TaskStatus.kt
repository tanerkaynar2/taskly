package com.taner.taskly.domain.model

enum class TaskStatus(val turkishName: String) {
    NOT_STARTED("Başlamadı"),
    IN_PROGRESS("Devam Ediyor"),
    COMPLETED("TAMAMLANDI");


    override fun toString(): String {
        return turkishName//super.toString()
    }

}