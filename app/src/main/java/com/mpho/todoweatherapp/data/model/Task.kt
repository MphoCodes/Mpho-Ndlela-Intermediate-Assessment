package com.mpho.todoweatherapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val completedAt: Date? = null
)
