package com.mpho.todoweatherapp.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mpho.todoweatherapp.data.local.TodoWeatherDatabase
import com.mpho.todoweatherapp.data.model.TaskPriority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

    private lateinit var database: TodoWeatherDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TodoWeatherDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = TaskRepository(database.taskDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun createTask_insertsTaskSuccessfully() = runBlocking {
        val taskId = repository.createTask(
            title = "New Task",
            description = "Task description",
            priority = TaskPriority.HIGH,
            deadline = null
        )
        
        assertThat(taskId).isGreaterThan(0)
        
        val tasks = repository.getAllTasks().first()
        assertThat(tasks).hasSize(1)
        assertThat(tasks[0].title).isEqualTo("New Task")
        assertThat(tasks[0].priority).isEqualTo(TaskPriority.HIGH)
    }

    @Test
    fun updateTask_modifiesExistingTask() = runBlocking {
        val taskId = repository.createTask(
            title = "Original",
            description = "Original description",
            priority = TaskPriority.LOW
        )
        
        repository.updateTask(
            taskId = taskId,
            title = "Updated",
            description = "Updated description",
            priority = TaskPriority.HIGH,
            deadline = null
        )
        
        val task = repository.getTaskById(taskId)
        assertThat(task).isNotNull()
        assertThat(task?.title).isEqualTo("Updated")
        assertThat(task?.description).isEqualTo("Updated description")
        assertThat(task?.priority).isEqualTo(TaskPriority.HIGH)
    }

    @Test
    fun toggleTaskCompletion_marksTaskAsCompleted() = runBlocking {
        val taskId = repository.createTask(
            title = "Task to Complete",
            description = "Will be completed"
        )
        
        var task = repository.getTaskById(taskId)
        assertThat(task?.isCompleted).isFalse()
        
        repository.toggleTaskCompletion(taskId)
        
        task = repository.getTaskById(taskId)
        assertThat(task?.isCompleted).isTrue()
        assertThat(task?.completedAt).isNotNull()
    }

    @Test
    fun toggleTaskCompletion_revertsCompletedTaskToPending() = runBlocking {
        val taskId = repository.createTask(
            title = "Task to Toggle",
            description = "Will be toggled twice"
        )
        
        repository.toggleTaskCompletion(taskId)
        var task = repository.getTaskById(taskId)
        assertThat(task?.isCompleted).isTrue()
        
        repository.toggleTaskCompletion(taskId)
        task = repository.getTaskById(taskId)
        assertThat(task?.isCompleted).isFalse()
        assertThat(task?.completedAt).isNull()
    }

    @Test
    fun deleteTask_removesTaskFromRepository() = runBlocking {
        val taskId = repository.createTask(
            title = "Task to Delete",
            description = "Will be removed"
        )
        
        var tasks = repository.getAllTasks().first()
        assertThat(tasks).hasSize(1)
        
        val task = repository.getTaskById(taskId)
        repository.deleteTask(task!!)
        
        tasks = repository.getAllTasks().first()
        assertThat(tasks).isEmpty()
    }

    @Test
    fun deleteCompletedTasks_removesOnlyCompletedTasks() = runBlocking {
        val pendingId = repository.createTask(
            title = "Pending Task",
            description = "Not completed"
        )
        
        val completedId1 = repository.createTask(
            title = "Completed Task 1",
            description = "Done"
        )
        repository.toggleTaskCompletion(completedId1)
        
        val completedId2 = repository.createTask(
            title = "Completed Task 2",
            description = "Done"
        )
        repository.toggleTaskCompletion(completedId2)
        
        repository.deleteCompletedTasks()
        
        val remainingTasks = repository.getAllTasks().first()
        assertThat(remainingTasks).hasSize(1)
        assertThat(remainingTasks[0].title).isEqualTo("Pending Task")
    }

    @Test
    fun getPendingTasks_returnsOnlyIncompleteTasks() = runBlocking {
        repository.createTask(title = "Pending 1", description = "Not done")
        val completedId = repository.createTask(title = "Completed", description = "Done")
        repository.toggleTaskCompletion(completedId)
        repository.createTask(title = "Pending 2", description = "Not done")
        
        val pendingTasks = repository.getPendingTasks().first()
        assertThat(pendingTasks).hasSize(2)
        assertThat(pendingTasks.map { it.title }).containsExactly("Pending 1", "Pending 2")
    }

    @Test
    fun getCompletedTasks_returnsOnlyCompletedTasks() = runBlocking {
        repository.createTask(title = "Pending", description = "Not done")
        
        val completedId1 = repository.createTask(title = "Completed 1", description = "Done")
        repository.toggleTaskCompletion(completedId1)
        
        val completedId2 = repository.createTask(title = "Completed 2", description = "Done")
        repository.toggleTaskCompletion(completedId2)
        
        val completedTasks = repository.getCompletedTasks().first()
        assertThat(completedTasks).hasSize(2)
        assertThat(completedTasks.map { it.title }).containsExactly("Completed 1", "Completed 2")
    }

    @Test
    fun createTaskWithDeadline_storesDeadlineCorrectly() = runBlocking {
        val deadline = Date(System.currentTimeMillis() + 86400000)
        
        val taskId = repository.createTask(
            title = "Task with Deadline",
            description = "Has a deadline",
            priority = TaskPriority.HIGH,
            deadline = deadline
        )
        
        val task = repository.getTaskById(taskId)
        assertThat(task?.deadline).isNotNull()
        assertThat(task?.deadline?.time).isEqualTo(deadline.time)
    }

}

