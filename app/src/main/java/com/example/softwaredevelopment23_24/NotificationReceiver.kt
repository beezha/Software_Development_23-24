package com.example.softwaredevelopment23_24

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.softwaredevelopment23_24.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "DAILY_REMINDER_CHANNEL_ID"
        val title = "It's almost midnight!"
        val message = "Complete a task to continue your streak!"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // You can request the permission here if needed, or handle the lack of permission in a different way
            return
        }

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build()) // Use a unique notification ID here
        }
    }
}