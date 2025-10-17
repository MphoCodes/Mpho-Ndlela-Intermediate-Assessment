package com.mpho.todoweatherapp.repository

import com.mpho.todoweatherapp.data.local.TaskDao
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import java.util.Date
class TaskRepository(
    private val taskDao: TaskDao
) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()

    suspend fun markTaskAsCompleted(taskId: Long) {
        taskDao.markTaskAsCompleted(taskId, Date().time)
    }

    suspend fun markTaskAsPending(taskId: Long) = taskDao.markTaskAsPending(taskId)

    suspend fun getTaskCount(): Int = taskDao.getTaskCount()

    suspend fun getCompletedTaskCount(): Int = taskDao.getCompletedTaskCount()

    suspend fun getPendingTaskCount(): Int = taskDao.getPendingTaskCount()

    suspend fun createTask(
        title: String,
        description: String,
        priority: TaskPriority = TaskPriority.MEDIUM
    ): Long {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            isCompleted = false,
            createdAt = Date(),
            completedAt = null
        )
        return insertTask(task)
    }

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
