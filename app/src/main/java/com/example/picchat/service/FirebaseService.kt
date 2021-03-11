package com.example.picchat.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.picchat.R
import com.example.picchat.other.Constants.CHANNEL_ID
import com.example.picchat.other.Constants.KEY_TOKEN
import com.example.picchat.repositories.MainRepository
import com.example.picchat.ui.auth.AuthActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class FirebaseService: FirebaseMessagingService() {

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        sharedPreferences.edit().putString(KEY_TOKEN, newToken).apply()
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // wake the screen after receiving the notification
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }
        //-------------------------------------

        val intent = Intent(this, AuthActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val vibrateArray = longArrayOf(1000)

        val notification = NotificationCompat.Builder(this@FirebaseService, CHANNEL_ID)
            .setContentTitle("Pic Chat")
            .setContentText("${message.data["title"]}${message.data["message"]}")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(vibrateArray)
            .build()

        notificationManager.notify(notificationID, notification)



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}