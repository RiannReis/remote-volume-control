package com.riannreis.rvcw.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.riannreis.rvcw.AuthKeyProvider
import com.riannreis.rvcw.server.WebServerService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("BootReceiver", "onReceive triggered with action: ${p1?.action}")
        if (Intent.ACTION_BOOT_COMPLETED == p1?.action) {
            val prefs = p0?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val autoStartEnabled = prefs?.getBoolean("autoStartEnabled", false) ?: false

            if (autoStartEnabled) {

                val port = prefs?.getInt("port", 9090) ?: 9090
                val authKey =
                    prefs?.getString("authKey", "3x@mplE_S3crEt_K3y!") ?: "3x@mplE_S3crEt_K3y!"
                AuthKeyProvider.secretKey = authKey

                prefs?.edit()?.putBoolean("isRunning", true)?.apply()

                Log.d("BootReceiver", "isRunning state saved as true")

                val intent = Intent(p0, WebServerService::class.java)
                    .apply {
                        putExtra("PORT_VALUE", port)
                        putExtra("AUTH_KEY", authKey)
                    }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    p0?.startForegroundService(intent)
                    Log.d(
                        "BootReceiver",
                        "Inside startForeGroundService: Auto-start enabled: $autoStartEnabled with port: $port and authKey: $authKey and isRunning: "
                    )
                } else {
                    p0?.startService(intent)
                }
                Log.d(
                    "BootReceiver",
                    "Outside: Auto-start enabled: $autoStartEnabled with port: $port and authKey: $authKey and isRunning: "
                )
            }
        }
    }
}