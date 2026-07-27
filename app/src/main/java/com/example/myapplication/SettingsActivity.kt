package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        window.statusBarColor = android.graphics.Color.WHITE

        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val arrow = findViewById<View>(R.id.setting_arrow)

        arrow.setOnClickListener {
            finish()
        }

        val btn = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchbtn)

        btn.setOnCheckedChangeListener { _, isChecked ->

        }

    }

}