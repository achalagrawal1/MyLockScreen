package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.service.LockScreenService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        Log.d(TAG, "onCreate: SettingsActivity created")

        window.statusBarColor = android.graphics.Color.WHITE
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val arrow = findViewById<View>(R.id.setting_arrow)
        arrow?.setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("LockScreenPrefs", Context.MODE_PRIVATE)
        val appPrefs = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)

        // Lock Screen Service Toggle Switch
        val btn = findViewById<SwitchCompat>(R.id.switchbtn)
        val isCurrentlyEnabled = prefs.getBoolean("isLockScreenEnabled", true)
        val showDateTime = appPrefs.getBoolean("showDateTime", true)
        btn?.isChecked = isCurrentlyEnabled

        btn?.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Switch toggled. isChecked=$isChecked")
            prefs.edit().putBoolean("isLockScreenEnabled", isChecked).apply()
            appPrefs.edit().putBoolean("showDateTime", isChecked).apply()

            val serviceIntent = Intent(this, LockScreenService::class.java)

            if (isChecked) {
                ContextCompat.startForegroundService(this, serviceIntent)
                Toast.makeText(this, "Lock screen & Date/Time enabled", Toast.LENGTH_SHORT).show()
            } else {
                stopService(serviceIntent)
                Toast.makeText(this, "Lock screen overlay disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Font Size Selection (Small, Medium, Large)
        val fontSmall = findViewById<TextView>(R.id.font_small)
        val fontMedium = findViewById<TextView>(R.id.font_medium)
        val fontLarge = findViewById<TextView>(R.id.font_large)

        val selectedFontSize = appPrefs.getString("fontSize", "medium") ?: "medium"
        updateFontSizeSelection(selectedFontSize, fontSmall, fontMedium, fontLarge)

        fontSmall?.setOnClickListener {
            appPrefs.edit().putString("fontSize", "small").apply()
            updateFontSizeSelection("small", fontSmall, fontMedium, fontLarge)
            Toast.makeText(this, "Font size set to Small", Toast.LENGTH_SHORT).show()
        }

        fontMedium?.setOnClickListener {
            appPrefs.edit().putString("fontSize", "medium").apply()
            updateFontSizeSelection("medium", fontSmall, fontMedium, fontLarge)
            Toast.makeText(this, "Font size set to Medium (Default)", Toast.LENGTH_SHORT).show()
        }

        fontLarge?.setOnClickListener {
            appPrefs.edit().putString("fontSize", "large").apply()
            updateFontSizeSelection("large", fontSmall, fontMedium, fontLarge)
            Toast.makeText(this, "Font size set to Large", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFontSizeSelection(
        selected: String,
        fontSmall: TextView?,
        fontMedium: TextView?,
        fontLarge: TextView?
    ) {
        fontSmall?.text = if (selected == "small") "small                 ✔" else "small"
        fontMedium?.text = if (selected == "medium") "medium             ✔" else "medium"
        fontLarge?.text = if (selected == "large") "large                 ✔" else "large"
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}