package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

class MainActivity : AppCompatActivity() {
    private val taskList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTaskBtn = findViewById<androidx.cardview.widget.CardView>(R.id.add_task_btn)

        addTaskBtn.setOnClickListener {
            showTaskDialog()
        }


        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val menuIcon = findViewById<LinearLayout>(R.id.menu_icon)

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener { item ->

            if (item.itemId == R.id.nav_settings) {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            else if (item.itemId == R.id.nav_profile) {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            else if (item.itemId == R.id.nav_logout) {

                FirebaseAuth.getInstance().signOut()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )

                finish()
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    private fun showTaskDialog() {

        val dialog = android.app.Dialog(this)

        dialog.setContentView(R.layout.add_task_dialog)

        val taskInput =
            dialog.findViewById<android.widget.EditText>(R.id.task_input)

        val saveBtn =
            dialog.findViewById<android.widget.Button>(R.id.save_task_btn)

        saveBtn.setOnClickListener {

            val task = taskInput.text.toString()

            if (task.isNotEmpty()) {

                android.widget.Toast.makeText(
                    this,
                    "Task Added",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }
}


