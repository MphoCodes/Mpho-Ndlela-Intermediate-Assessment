package com.mpho.todoweatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpho.todoweatherapp.data.model.Task
import com.mpho.todoweatherapp.data.model.TaskPriority
import com.mpho.todoweatherapp.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
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
    
    fun createTask(
        title: String,
        description: String,
        priority: TaskPriority = TaskPriority.MEDIUM
    ) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Task title cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                taskRepository.createTask(title.trim(), description.trim(), priority)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class TaskUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

data class TaskCounts(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0
)
