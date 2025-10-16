package com.mpho.todoweatherapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a TODO task
 *
 * @param id Unique identifier for the task
 * @param title The title/name of the task
 * @param description Detailed description of the task
 * @param isCompleted Whether the task has been completed
 * @param createdAt When the task was created
 * @param completedAt When the task was completed (null if not completed)
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val completedAt: Date? = null
)
