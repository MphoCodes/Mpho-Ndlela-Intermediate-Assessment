package com.mpho.todoweatherapp.data.local

import androidx.room.*
import com.mpho.todoweatherapp.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Task operations
 * Provides methods to interact with the tasks table in the database
 */
@Dao
interface TaskDao {
    
    /**
     * Get all tasks ordered by creation date (newest first)
     * Returns a Flow for reactive updates
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Get all completed tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    /**
     * Get all pending (not completed) tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingTasks(): Flow<List<Task>>
    
    /**
     * Get a specific task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    /**
     * Insert a new task
     * @return the ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    /**
     * Insert multiple tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Update an existing task
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Delete a specific task
     */
    @Delete
    suspend fun deleteTask(task: Task)
    
    /**
     * Delete a task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
    
    /**
     * Delete all completed tasks
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
    
    /**
     * Delete all tasks
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    /**
     * Mark a task as completed
     */
    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Long, completedAt: Long)
    
    /**
     * Mark a task as pending (not completed)
     */
    @Query("UPDATE tasks SET isCompleted = 0, completedAt = NULL WHERE id = :taskId")
    suspend fun markTaskAsPending(taskId: Long)
    
    /**
     * Get count of all tasks
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
    
    /**
     * Get count of completed tasks
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTaskCount(): Int
    
    /**
     * Get count of pending tasks
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getPendingTaskCount(): Int
}
