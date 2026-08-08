package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.LockScreenActivity
import com.example.myapplication.R

class LockScreenService : Service() {

    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: LockScreenService created")
        startForegroundServiceNotification()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Service started with startId=$startId")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: LockScreenService destroyed")
        unregisterScreenReceiver()
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            screenReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d(TAG, "onReceive: Broadcast received with action=${intent?.action}")
                    if (intent?.action == Intent.ACTION_SCREEN_ON) {
                        val prefs = getSharedPreferences("LockScreenPrefs", Context.MODE_PRIVATE)
                        val isEnabled = prefs.getBoolean("isLockScreenEnabled", true)

                        Log.d(TAG, "Screen ON detected. isLockScreenEnabled=$isEnabled")

                        if (isEnabled && context != null) {
                            Log.d(TAG, "Attempting to launch LockScreenActivity from background")
                            val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            try {
                                context.startActivity(lockIntent)
                                Log.d(TAG, "LockScreenActivity launch intent sent successfully")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error launching LockScreenActivity from background", e)
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
            registerReceiver(screenReceiver, filter)
            Log.d(TAG, "registerScreenReceiver: Intent.ACTION_SCREEN_ON receiver registered")
        }
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
                Log.d(TAG, "unregisterScreenReceiver: Receiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering screenReceiver", e)
            }
            screenReceiver = null
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "lock_screen_service_channel"
        val channelName = "Lock Screen Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Lock Screen Tasks")
            .setContentText("Listening for screen wake events")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "startForegroundServiceNotification: Foreground notification displayed")
    }

    companion object {
        private const val TAG = "LockScreenService"
        private const val NOTIFICATION_ID = 1001
    }
}
