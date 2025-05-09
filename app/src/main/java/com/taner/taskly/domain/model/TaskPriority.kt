package com.taner.taskly.domain.model

enum class TaskPriority(val turkishName: String) {
    LOW("DÜŞÜK"),
    MEDIUM("NORMAL"),
    HIGH("ÖNEMLİ");


    override fun toString(): String {
        return turkishName//super.toString()
    }

}