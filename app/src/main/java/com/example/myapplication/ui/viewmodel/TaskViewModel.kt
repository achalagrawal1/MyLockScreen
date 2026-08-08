package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Task
import com.example.myapplication.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val todayTasks: LiveData<List<Task>> = repository.getTodayTasks().asLiveData()
    val yesterdayTasks: LiveData<List<Task>> = repository.getYesterdayTasks().asLiveData()
    val allTasks: LiveData<List<Task>> = repository.getAllTasks().asLiveData()

    fun addTask(
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        slotIndex: Int = 0,
        alarmTime: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                slotIndex = slotIndex,
                alarmTime = alarmTime,
                dateMillis = System.currentTimeMillis()
            )
            repository.insertTask(newTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
