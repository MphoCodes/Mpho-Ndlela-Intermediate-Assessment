package com.mpho.todoweatherapp.repository

import com.mpho.todoweatherapp.data.local.TaskDao
import com.mpho.todoweatherapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Task data operations
 * 
 * This repository handles all task-related data operations,
 * serving as a single source of truth for task data.
 * Currently uses only local data (Room database).
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    
    /**
     * Get all tasks as a Flow for reactive updates
     */
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    /**
     * Get completed tasks as a Flow
     */
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    
    /**
     * Get pending (not completed) tasks as a Flow
     */
    fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()
    
    /**
     * Get a specific task by ID
     */
    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)
    
    /**
     * Insert a new task
     * @return the ID of the inserted task
     */
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    
    /**
     * Insert multiple tasks
     */
    suspend fun insertTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)
    
    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    /**
     * Delete a specific task
     */
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    /**
     * Delete a task by ID
     */
    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)
    
    /**
     * Delete all completed tasks
     */
    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()
    
    /**
     * Delete all tasks
     */
    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()
    
    /**
     * Mark a task as completed
     */
    suspend fun markTaskAsCompleted(taskId: Long) {
        taskDao.markTaskAsCompleted(taskId, Date().time)
    }
    
    /**
     * Mark a task as pending (not completed)
     */
    suspend fun markTaskAsPending(taskId: Long) = taskDao.markTaskAsPending(taskId)
    
    /**
     * Get count of all tasks
     */
    suspend fun getTaskCount(): Int = taskDao.getTaskCount()
    
    /**
     * Get count of completed tasks
     */
    suspend fun getCompletedTaskCount(): Int = taskDao.getCompletedTaskCount()
    
    /**
     * Get count of pending tasks
     */
    suspend fun getPendingTaskCount(): Int = taskDao.getPendingTaskCount()
    
    /**
     * Create a new task with title and description
     */
    suspend fun createTask(title: String, description: String): Long {
        val task = Task(
            title = title,
            description = description,
            isCompleted = false,
            createdAt = Date(),
            completedAt = null
        )
        return insertTask(task)
    }
    
    /**
     * Toggle task completion status
     */
    suspend fun toggleTaskCompletion(taskId: Long) {
        val task = getTaskById(taskId)
        task?.let {
            if (it.isCompleted) {
                markTaskAsPending(taskId)
            } else {
                markTaskAsCompleted(taskId)
            }
        }
    }
}
