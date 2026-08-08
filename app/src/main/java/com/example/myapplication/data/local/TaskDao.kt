package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task): Long

    @Update
    fun updateTask(task: Task): Int

    @Delete
    fun deleteTask(task: Task): Int

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, id DESC")
    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dateMillis >= :startOfYesterday AND dateMillis <= :endOfYesterday ORDER BY isCompleted ASC, id DESC")
    fun getYesterdayTasks(startOfYesterday: Long, endOfYesterday: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>
}
