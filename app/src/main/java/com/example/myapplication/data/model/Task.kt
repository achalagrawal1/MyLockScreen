package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val dateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val alarmTime: String? = null, // e.g. "8:00 AM"
    val slotIndex: Int = 0 // 0 to 6
)
