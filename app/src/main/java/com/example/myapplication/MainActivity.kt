package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.model.Task
import com.example.myapplication.data.repository.TaskRepository
import com.example.myapplication.service.AlarmScheduler
import com.example.myapplication.ui.adapter.TaskAdapter
import com.example.myapplication.ui.viewmodel.TaskViewModel
import com.example.myapplication.ui.viewmodel.TaskViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var todayAdapter: TaskAdapter
    private lateinit var plannerCard: View
    private var isPreviousDayView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: MainActivity created")

        // Initialize Database, Repository, and ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        checkOverlayPermission()
        startLockScreenService()

        // UI Components from existing XML
        plannerCard = findViewById(R.id.plannerCard)
        val recyclerView = findViewById<RecyclerView>(R.id.todayTasksRecyclerView)
        val doneBtn = findViewById<Button>(R.id.doneBtn)
        val addBtn = findViewById<ImageButton>(R.id.addBtn)
        val dotIndicator = findViewById<LinearLayout>(R.id.dotIndicator)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuIcon = findViewById<LinearLayout>(R.id.menu_icon)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val todayHeader = findViewById<TextView>(R.id.todayHeader)

        // Populate Navigation Drawer Header User Name from Firebase Auth
        val headerView = navigationView.getHeaderView(0)
        val profileNameView = headerView?.findViewById<TextView>(R.id.profile_name)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "User"
        profileNameView?.text = displayName

        // Setup Task Adapter using existing item_task.xml views
        todayAdapter = TaskAdapter(
            isReadOnly = false,
            onSaveTask = { slotIndex, existingTask, title ->
                if (existingTask != null) {
                    val updated = existingTask.copy(
                        title = title,
                        slotIndex = slotIndex,
                        dateMillis = System.currentTimeMillis()
                    )
                    viewModel.updateTask(updated)
                    if (!updated.alarmTime.isNullOrBlank()) {
                        AlarmScheduler.scheduleAlarm(this, updated)
                    }
                } else {
                    val newTask = Task(
                        title = title,
                        slotIndex = slotIndex,
                        dateMillis = System.currentTimeMillis()
                    )
                    viewModel.addTask(title = title, slotIndex = slotIndex)
                }
            },
            onDeleteTask = { task ->
                viewModel.deleteTask(task)
                AlarmScheduler.cancelAlarm(this, task)
                Toast.makeText(this, "Task Deleted", Toast.LENGTH_SHORT).show()
            },
            onSetAlarmTime = { task, timeStr ->
                val updated = task.copy(alarmTime = timeStr)
                viewModel.updateTask(updated)
                AlarmScheduler.scheduleAlarm(this, updated)
                Toast.makeText(this, "Reminder set for $timeStr", Toast.LENGTH_SHORT).show()
            },
            onNextSlot = { currentSlotIndex ->
                todayAdapter.inlineEditingSlotIndex = currentSlotIndex + 1
                todayAdapter.notifyDataSetChanged()
            },
            onEditStarted = {
                plannerCard.animate().translationY(-50f).setDuration(200).start()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = todayAdapter

        // Observe Today's Tasks
        viewModel.todayTasks.observe(this) { tasks ->
            if (!isPreviousDayView) {
                todayAdapter.updateTasks(tasks ?: emptyList())
            }
        }

        // Observe Yesterday's / Previous Day's Tasks
        viewModel.yesterdayTasks.observe(this) { tasks ->
            if (isPreviousDayView) {
                todayAdapter.updateTasks(tasks ?: emptyList())
            }
        }

        // "+" Button Press Handler -> Moves CardView upward & starts task entry
        addBtn.setOnClickListener {
            if (isPreviousDayView) {
                isPreviousDayView = false
                todayHeader.text = "Today"
                todayAdapter.updateTasks(viewModel.todayTasks.value ?: emptyList())
            }
            plannerCard.animate().translationY(-60f).setDuration(250).start()
            todayAdapter.inlineEditingSlotIndex = 0
            todayAdapter.notifyDataSetChanged()
        }

        // "Done" Button Press Handler -> Saves tasks, returns CardView to normal position, exits task entry
        doneBtn.setOnClickListener {
            todayAdapter.inlineEditingSlotIndex = -1
            todayAdapter.notifyDataSetChanged()
            plannerCard.animate().translationY(0f).setDuration(250).start()

            // Schedule alarms for all today's tasks with set reminder times
            val currentTasks = viewModel.todayTasks.value ?: emptyList()
            currentTasks.forEach { task ->
                if (!task.alarmTime.isNullOrBlank()) {
                    AlarmScheduler.scheduleAlarm(this, task)
                }
            }

            Toast.makeText(this, "Tasks Saved", Toast.LENGTH_SHORT).show()
        }

        // Orange Dot Navigation Concept -> Toggles between Today & Previous Day
        dotIndicator.setOnClickListener {
            isPreviousDayView = !isPreviousDayView
            if (isPreviousDayView) {
                todayHeader.text = "Previous Day"
                todayAdapter.updateTasks(viewModel.yesterdayTasks.value ?: emptyList())
                Toast.makeText(this, "Showing Previous Day tasks", Toast.LENGTH_SHORT).show()
            } else {
                todayHeader.text = "Today"
                todayAdapter.updateTasks(viewModel.todayTasks.value ?: emptyList())
                Toast.makeText(this, "Showing Today's tasks", Toast.LENGTH_SHORT).show()
            }
        }

        // Drawer Menu Click
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation Drawer Item Selection
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission (SYSTEM_ALERT_WINDOW) not granted. Requesting user permission.")
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Log.d(TAG, "Overlay permission (SYSTEM_ALERT_WINDOW) is granted.")
            }
        }
    }

    private fun startLockScreenService() {
        val prefs = getSharedPreferences("LockScreenPrefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("isLockScreenEnabled", true)
        Log.d(TAG, "startLockScreenService: isLockScreenEnabled=$isEnabled")
        if (isEnabled) {
            val serviceIntent = Intent(this, com.example.myapplication.service.LockScreenService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
            Log.d(TAG, "startLockScreenService: Called startForegroundService")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}


