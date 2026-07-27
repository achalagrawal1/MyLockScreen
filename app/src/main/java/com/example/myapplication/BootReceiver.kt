package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_SCREEN_ON) {

            val i = Intent(context, LockScreenActivity::class.java)

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(i)
        }
    }
}