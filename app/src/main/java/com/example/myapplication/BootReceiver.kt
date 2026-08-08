package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.myapplication.service.LockScreenService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: Received broadcast action=${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("LockScreenPrefs", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("isLockScreenEnabled", true)

            Log.d(TAG, "Boot completed. isLockScreenEnabled=$isEnabled")

            if (isEnabled) {
                val serviceIntent = Intent(context, LockScreenService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
                Log.d(TAG, "Started LockScreenService from BootReceiver")
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}