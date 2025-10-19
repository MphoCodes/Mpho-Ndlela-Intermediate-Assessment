package com.mpho.todoweatherapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.data.model.TaskPriority
import com.mpho.todoweatherapp.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Date
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: TaskRepository

    private lateinit var viewModel: TaskViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        `when`(repository.getAllTasks()).thenReturn(flowOf(emptyList()))
        `when`(repository.getPendingTasks()).thenReturn(flowOf(emptyList()))
        `when`(repository.getCompletedTasks()).thenReturn(flowOf(emptyList()))
        
        viewModel = TaskViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createTask with valid data calls repository`() = runTest {
        `when`(repository.createTask(any(), any(), any(), any())).thenReturn(1L)
        
        viewModel.createTask(
            title = "Test Task",
            description = "Test Description",
            priority = TaskPriority.HIGH,
            deadline = null
        )
        
        advanceUntilIdle()
        
        verify(repository).createTask(
            title = "Test Task",
            description = "Test Description",
            priority = TaskPriority.HIGH,
            deadline = null
        )
        
        assertThat(viewModel.uiState.value.message).isEqualTo("Task created successfully")
    }

    @Test
    fun `createTask with empty title shows error`() = runTest {
        viewModel.createTask(
            title = "",
            description = "Test Description",
            priority = TaskPriority.MEDIUM
        )
        
        advanceUntilIdle()
        
        verify(repository, never()).createTask(any(), any(), any(), any())
        assertThat(viewModel.uiState.value.error).isEqualTo("Task title cannot be empty")
    }

    @Test
    fun `createTask with blank title shows error`() = runTest {
        viewModel.createTask(
            title = "   ",
            description = "Test Description",
            priority = TaskPriority.LOW
        )
        
        advanceUntilIdle()
        
        verify(repository, never()).createTask(any(), any(), any(), any())
        assertThat(viewModel.uiState.value.error).isEqualTo("Task title cannot be empty")
    }

    @Test
    fun `updateTask calls repository with correct parameters`() = runTest {
        val taskId = 1L
        val task = Task(
            id = taskId,
            title = "Original",
            description = "Original description",
            priority = TaskPriority.LOW
        )
        
        `when`(repository.getTaskById(taskId)).thenReturn(task)
        
        viewModel.updateTask(
            taskId = taskId,
            title = "Updated",
            description = "Updated description",
            priority = TaskPriority.HIGH,
            deadline = null
        )
        
        advanceUntilIdle()
        
        verify(repository).updateTask(taskId, "Updated", "Updated description", TaskPriority.HIGH, null)
        assertThat(viewModel.uiState.value.message).isEqualTo("Task updated successfully")
    }

    @Test
    fun `deleteTask calls repository and shows success message`() = runTest {
        val task = Task(
            id = 1L,
            title = "Task to Delete",
            description = "Will be deleted"
        )
        
        viewModel.deleteTask(task)
        advanceUntilIdle()
        
        verify(repository).deleteTask(task)
        assertThat(viewModel.uiState.value.message).isEqualTo("Task deleted successfully")
    }

    @Test
    fun `toggleTaskCompletion calls repository`() = runTest {
        val taskId = 1L
        
        viewModel.toggleTaskCompletion(taskId)
        advanceUntilIdle()
        
        verify(repository).toggleTaskCompletion(taskId)
    }

    @Test
    fun `deleteCompletedTasks calls repository and shows success message`() = runTest {
        viewModel.deleteCompletedTasks()
        advanceUntilIdle()
        
        verify(repository).deleteCompletedTasks()
        assertThat(viewModel.uiState.value.message).isEqualTo("Completed tasks deleted successfully")
    }

    @Test
    fun `clearError resets error state`() = runTest {
        viewModel.createTask("", "Description")
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.error).isNotNull()
        
        viewModel.clearError()
        
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `clearMessage resets message state`() = runTest {
        val task = Task(id = 1L, title = "Test", description = "Test")
        viewModel.deleteTask(task)
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.message).isNotNull()
        
        viewModel.clearMessage()
        
        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun `repository error is handled gracefully`() = runTest {
        `when`(repository.createTask(any(), any(), any(), any()))
            .thenThrow(RuntimeException("Database error"))
        
        viewModel.createTask(
            title = "Test Task",
            description = "Test Description"
        )
        
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.error).contains("Failed to create task")
    }

    @Test
    fun `loading state is set during task creation`() = runTest {
        `when`(repository.createTask(any(), any(), any(), any())).thenReturn(1L)
        
        viewModel.createTask(
            title = "Test Task",
            description = "Test Description"
        )
        
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}

private fun <T> any(): T {
    org.mockito.Mockito.any<T>()
    return null as T
}

