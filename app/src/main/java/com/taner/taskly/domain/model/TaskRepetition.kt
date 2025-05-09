package com.taner.taskly.domain.model

enum class TaskRepetition(val turkishName: String)  {
    DAILY("Günlük"),
    WEEKLY("HAFTALIK"),
    MONTHLY("AYLIK"),
    NONE("TEKRAR YOK");

    override fun toString(): String {
        return turkishName//super.toString()
    }

}