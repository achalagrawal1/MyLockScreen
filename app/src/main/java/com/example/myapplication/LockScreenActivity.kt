package com.example.myapplication

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.TaskRepository
import com.example.myapplication.ui.adapter.LockTaskAdapter
import com.example.myapplication.ui.viewmodel.TaskViewModel
import com.example.myapplication.ui.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockScreenActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: LockTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show window over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_lock_screen)

        // Read Show Date & Time preference from Settings
        val appPrefs = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        val showDateTime = appPrefs.getBoolean("showDateTime", true)

        val clockHeader = findViewById<View>(R.id.clock_header)
        val lockDateText = findViewById<TextView>(R.id.lockDateText)

        if (showDateTime) {

            clockHeader.visibility = View.VISIBLE

            val dateFormat = SimpleDateFormat(
                "EEEE, d MMMM",
                Locale.getDefault()
            )

            lockDateText.text = dateFormat.format(Date())

        } else {

            clockHeader.visibility = View.GONE

        }

        // Initialize Database, Repository, and ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Setup Lock Tasks RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.lockTasksRecyclerView)
        adapter = LockTaskAdapter(
            onToggleCompletion = { task ->
                viewModel.toggleTaskCompletion(task)
            }
        )
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter

        // Observe Today's Tasks
        viewModel.todayTasks.observe(this) { tasks ->
            adapter.submitList(tasks ?: emptyList())
        }

        val lockImage = findViewById<View>(R.id.lockImage)
        lockImage?.setOnClickListener {
            unlockPhoneAndDismiss()
        }
    }

    private fun unlockPhoneAndDismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        finish()
    }
}