package com.mpho.todoweatherapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.data.model.TaskPriority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TodoWeatherDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TodoWeatherDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTask_retrievesTaskSuccessfully() = runBlocking {
        val task = Task(
            title = "Test Task",
            description = "Testing insert operation",
            priority = TaskPriority.HIGH
        )
        
        val taskId = dao.insertTask(task)
        
        val allTasks = dao.getAllTasks().first()
        assertThat(allTasks).isNotEmpty()
        assertThat(allTasks[0].title).isEqualTo("Test Task")
        assertThat(allTasks[0].description).isEqualTo("Testing insert operation")
        assertThat(allTasks[0].priority).isEqualTo(TaskPriority.HIGH)
        assertThat(taskId).isGreaterThan(0)
    }

    @Test
    fun insertMultipleTasks_retrievesAllTasks() = runBlocking {
        val tasks = listOf(
            Task(title = "Task 1", description = "First task", priority = TaskPriority.HIGH),
            Task(title = "Task 2", description = "Second task", priority = TaskPriority.MEDIUM),
            Task(title = "Task 3", description = "Third task", priority = TaskPriority.LOW)
        )
        
        dao.insertTasks(tasks)
        
        val allTasks = dao.getAllTasks().first()
        assertThat(allTasks).hasSize(3)
        assertThat(allTasks.map { it.title }).containsExactly("Task 1", "Task 2", "Task 3")
    }

    @Test
    fun updateTask_changesTaskProperties() = runBlocking {
        val task = Task(
            title = "Original Title",
            description = "Original Description",
            priority = TaskPriority.LOW
        )
        val taskId = dao.insertTask(task)
        
        val retrievedTask = dao.getTaskById(taskId)
        assertThat(retrievedTask).isNotNull()
        
        val updatedTask = retrievedTask!!.copy(
            title = "Updated Title",
            description = "Updated Description",
            priority = TaskPriority.HIGH
        )
        dao.updateTask(updatedTask)
        
        val finalTask = dao.getTaskById(taskId)
        assertThat(finalTask?.title).isEqualTo("Updated Title")
        assertThat(finalTask?.description).isEqualTo("Updated Description")
        assertThat(finalTask?.priority).isEqualTo(TaskPriority.HIGH)
    }

    @Test
    fun deleteTask_removesTaskFromDatabase() = runBlocking {
        val task = Task(
            title = "Task to Delete",
            description = "This will be deleted"
        )
        dao.insertTask(task)
        
        var allTasks = dao.getAllTasks().first()
        assertThat(allTasks).hasSize(1)
        
        dao.deleteTask(allTasks[0])
        
        allTasks = dao.getAllTasks().first()
        assertThat(allTasks).isEmpty()
    }

    @Test
    fun markTaskAsCompleted_updatesCompletionStatus() = runBlocking {
        val task = Task(
            title = "Task to Complete",
            description = "Will be marked complete"
        )
        val taskId = dao.insertTask(task)
        
        dao.markTaskAsCompleted(taskId, System.currentTimeMillis())
        
        val completedTask = dao.getTaskById(taskId)
        assertThat(completedTask?.isCompleted).isTrue()
        assertThat(completedTask?.completedAt).isNotNull()
    }

    @Test
    fun markTaskAsPending_revertsCompletionStatus() = runBlocking {
        val task = Task(
            title = "Completed Task",
            description = "Will be reverted to pending",
            isCompleted = true,
            completedAt = Date()
        )
        val taskId = dao.insertTask(task)
        
        dao.markTaskAsPending(taskId)
        
        val pendingTask = dao.getTaskById(taskId)
        assertThat(pendingTask?.isCompleted).isFalse()
        assertThat(pendingTask?.completedAt).isNull()
    }

    @Test
    fun getPendingTasks_returnsOnlyIncompleteTasks() = runBlocking {
        val tasks = listOf(
            Task(title = "Pending 1", description = "Not done", isCompleted = false),
            Task(title = "Completed 1", description = "Done", isCompleted = true),
            Task(title = "Pending 2", description = "Not done", isCompleted = false)
        )
        dao.insertTasks(tasks)
        
        val pendingTasks = dao.getPendingTasks().first()
        assertThat(pendingTasks).hasSize(2)
        assertThat(pendingTasks.map { it.title }).containsExactly("Pending 1", "Pending 2")
    }

    @Test
    fun getCompletedTasks_returnsOnlyCompletedTasks() = runBlocking {
        val tasks = listOf(
            Task(title = "Pending 1", description = "Not done", isCompleted = false),
            Task(title = "Completed 1", description = "Done", isCompleted = true, completedAt = Date()),
            Task(title = "Completed 2", description = "Done", isCompleted = true, completedAt = Date())
        )
        dao.insertTasks(tasks)
        
        val completedTasks = dao.getCompletedTasks().first()
        assertThat(completedTasks).hasSize(2)
        assertThat(completedTasks.map { it.title }).containsExactly("Completed 1", "Completed 2")
    }

    @Test
    fun pendingTasks_sortedByPriorityThenDeadline() = runBlocking {
        val now = System.currentTimeMillis()
        val tomorrow = Date(now + 86400000)
        val nextWeek = Date(now + 604800000)
        
        val tasks = listOf(
            Task(title = "Low Priority", description = "Low", priority = TaskPriority.LOW, deadline = tomorrow),
            Task(title = "High Priority Soon", description = "High", priority = TaskPriority.HIGH, deadline = tomorrow),
            Task(title = "Medium Priority", description = "Medium", priority = TaskPriority.MEDIUM, deadline = nextWeek),
            Task(title = "High Priority Later", description = "High", priority = TaskPriority.HIGH, deadline = nextWeek)
        )
        dao.insertTasks(tasks)
        
        val pendingTasks = dao.getPendingTasks().first()
        
        assertThat(pendingTasks[0].title).isEqualTo("High Priority Soon")
        assertThat(pendingTasks[1].title).isEqualTo("High Priority Later")
        assertThat(pendingTasks[2].title).isEqualTo("Medium Priority")
        assertThat(pendingTasks[3].title).isEqualTo("Low Priority")
    }

    @Test
    fun deleteCompletedTasks_removesOnlyCompletedTasks() = runBlocking {
        val tasks = listOf(
            Task(title = "Pending", description = "Not done", isCompleted = false),
            Task(title = "Completed 1", description = "Done", isCompleted = true),
            Task(title = "Completed 2", description = "Done", isCompleted = true)
        )
        dao.insertTasks(tasks)
        
        dao.deleteCompletedTasks()
        
        val remainingTasks = dao.getAllTasks().first()
        assertThat(remainingTasks).hasSize(1)
        assertThat(remainingTasks[0].title).isEqualTo("Pending")
    }

    @Test
    fun getTaskCount_returnsCorrectCount() = runBlocking {
        val tasks = listOf(
            Task(title = "Task 1", description = "First"),
            Task(title = "Task 2", description = "Second"),
            Task(title = "Task 3", description = "Third")
        )
        dao.insertTasks(tasks)
        
        val count = dao.getTaskCount()
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun getCompletedTaskCount_returnsCorrectCount() = runBlocking {
        val tasks = listOf(
            Task(title = "Pending", description = "Not done", isCompleted = false),
            Task(title = "Completed 1", description = "Done", isCompleted = true),
            Task(title = "Completed 2", description = "Done", isCompleted = true)
        )
        dao.insertTasks(tasks)
        
        val count = dao.getCompletedTaskCount()
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun getPendingTaskCount_returnsCorrectCount() = runBlocking {
        val tasks = listOf(
            Task(title = "Pending 1", description = "Not done", isCompleted = false),
            Task(title = "Pending 2", description = "Not done", isCompleted = false),
            Task(title = "Completed", description = "Done", isCompleted = true)
        )
        dao.insertTasks(tasks)
        
        val count = dao.getPendingTaskCount()
        assertThat(count).isEqualTo(2)
    }
}

