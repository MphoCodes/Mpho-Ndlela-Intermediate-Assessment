package com.mpho.todoweatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Task-related UI state and business logic
 * 
 * Handles all task operations including CRUD operations,
 * filtering, and UI state management
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    // UI State for task operations
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    // All tasks from repository
    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Pending tasks (not completed)
    val pendingTasks: StateFlow<List<Task>> = taskRepository.getPendingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Completed tasks
    val completedTasks: StateFlow<List<Task>> = taskRepository.getCompletedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Task counts for statistics
    val taskCounts: StateFlow<TaskCounts> = combine(
        pendingTasks,
        completedTasks
    ) { pending, completed ->
        TaskCounts(
            total = pending.size + completed.size,
            pending = pending.size,
            completed = completed.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskCounts()
    )
    
    /**
     * Create a new task
     */
    fun createTask(title: String, description: String) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Task title cannot be empty"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                taskRepository.createTask(title.trim(), description.trim())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Task created successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create task: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Toggle task completion status
     */
    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update task: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete a specific task
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                _uiState.value = _uiState.value.copy(
                    message = "Task deleted successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete task: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete all completed tasks
     */
    fun deleteCompletedTasks() {
        viewModelScope.launch {
            try {
                taskRepository.deleteCompletedTasks()
                _uiState.value = _uiState.value.copy(
                    message = "Completed tasks deleted successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete completed tasks: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear success message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

/**
 * UI State for Task operations
 */
data class TaskUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * Data class for task counts/statistics
 */
data class TaskCounts(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0
)
