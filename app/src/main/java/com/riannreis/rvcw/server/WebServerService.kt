package com.riannreis.rvcw.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.riannreis.rvcw.AuthKeyProvider
import com.riannreis.rvcw.R

class WebServerService : Service() {

    private lateinit var webServer: WebServer
    private var portValue = 9090
    private var authKey = AuthKeyProvider.secretKey

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Volume control server",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        webServer = WebServer(this, audioManager)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        portValue = intent?.getIntExtra("PORT_VALUE", 9090) ?: 9090
        authKey = intent?.getStringExtra("AUTH_KEY") ?: AuthKeyProvider.secretKey

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.volume_control_active))
            .setContentText(getString(R.string.server_running_in_the_background))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(NOTIFICATION_ID, notification)


        webServer.startServer(portValue, authKey)
        return START_STICKY
    }

    fun notifyServerState(isRunning: Boolean) {
        val intent = Intent("SERVER_STATE_UPDATE")
        intent.putExtra("IS_RUNNING", isRunning)
        sendBroadcast(intent)
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer.stopServer()
    }

    companion object {
        const val CHANNEL_ID = "VolumeControlChannel"
        const val NOTIFICATION_ID = 1
    }
}