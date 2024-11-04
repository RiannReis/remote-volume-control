package com.riannreis.rvcw.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.riannreis.rvcw.AuthKeyProvider
import com.riannreis.rvcw.R
import com.riannreis.rvcw.dialogs.InputAuthKeyDialogFragment
import com.riannreis.rvcw.dialogs.InputPortDialogFragment
import com.riannreis.rvcw.server.WebServerService
import java.util.Locale
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(), InputPortDialogFragment.PortDialogListener, InputAuthKeyDialogFragment.AuthDialogListener {

    private lateinit var openDialogPortBtn: Button
    private lateinit var openDialogAuthBtn: Button
    private lateinit var startOrEndRemoteControlBtn: Button
    private lateinit var closeBtn: Button
    private lateinit var txtCloseDesc: TextView
    private lateinit var txtDesc: TextView
    private lateinit var webLink: TextView

    private var serverIpIsPrivate: Boolean = true
    private var portValue: Int = 9090
    private var isRunning: Boolean = false
    private var localIp: String? = null
    private var rvcwURL: String? = null

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isgranted: Boolean ->
        if (isgranted){

        } else {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        localIp = getLocalIpAddress()

        portValue = loadPort()

        AuthKeyProvider.secretKey = loadAuthKey()

        rvcwURL = "http://$localIp:$portValue/?authKey=${AuthKeyProvider.secretKey}"

        checkAndRequestNotificationPermission()

        openDialogPortBtn = findViewById(R.id.btn_dialog_choose_port)
        openDialogAuthBtn = findViewById(R.id.btn_dialog_basic_auth)
        startOrEndRemoteControlBtn = findViewById(R.id.btn_start_or_end_remote_control)
        webLink = findViewById(R.id.web_link)
        closeBtn = findViewById(R.id.btn_close)
        txtCloseDesc = findViewById(R.id.txt_close_desc)
        txtDesc = findViewById(R.id.txt_desc)

        serverIpIsPrivate = isPrivateAddress(localIp)

        // Main Button (Enable / Disable)

        startOrEndRemoteControlBtn.setOnClickListener {
            if (isRunning){
                stopRemoteControlService()
            } else {
                startRemoteControlService()
            }

            isRunning = !isRunning
            updateActivity()


            Log.d("WebServer", "Server state toggled. Running: $isRunning")
        }


        // Dialogs

        openDialogPortBtn.setOnClickListener {
            InputPortDialogFragment().show(supportFragmentManager, "INPUT_PORT")
        }

        openDialogAuthBtn.setOnClickListener {
            InputAuthKeyDialogFragment().show(supportFragmentManager, "INPUT_SECRET_KEY")
        }

        // Close

        closeBtn.setOnClickListener {
            finish()
        }

    }

    private val serverStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isRunning = intent?.getBooleanExtra("IS_RUNNING", false) ?: false
            this@MainActivity.isRunning = isRunning
            updateActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(serverStateReceiver, IntentFilter("SERVER_STATE_UPDATE"),
                    RECEIVER_NOT_EXPORTED
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(serverStateReceiver)
    }


    private fun startRemoteControlService() {
        val intent = Intent(this, WebServerService::class.java)
        intent.putExtra("PORT_VALUE", portValue)
        intent.putExtra("SERVER_IP", localIp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateActivity()

    }

    private fun stopRemoteControlService() {
        stopService(Intent(this, WebServerService::class.java))
        updateActivity()
    }


    private fun updateActivity() {
        if (isRunning) {
            if (serverIpIsPrivate) {
                startOrEndRemoteControlBtn.setText(R.string.running)
                txtDesc.setText(R.string.web_remote_volume_control_enabled)
                webLink.visibility = View.VISIBLE
                webLink.text = rvcwURL
                invisibleButtons(openDialogPortBtn, openDialogAuthBtn)
            } else {
                webLink.visibility = View.VISIBLE
                webLink.setText(R.string.verify_local_network_connection)
                txtDesc.setText(R.string.unable_to_find_local_address)
                txtCloseDesc.setText(R.string.about_private_limitation)
            }
        } else {
            startOrEndRemoteControlBtn.setText(R.string.start)
            if (serverIpIsPrivate) {
                txtDesc.setText(R.string.web_remote_volume_control_disabled)
                webLink.visibility = View.GONE
                visibleButtons(openDialogPortBtn, openDialogAuthBtn)
                txtCloseDesc.setText(R.string.close_description)
            } else {
                webLink.visibility = View.VISIBLE
                webLink.setText(R.string.verify_local_network_connection)
                txtDesc.setText(R.string.unable_to_find_local_address)
                txtCloseDesc.setText(R.string.about_private_limitation)
            }
        }
    }


    private fun visibleButtons(btn1: Button, btn2: Button) {
        btn1.visibility = View.VISIBLE
        btn2.visibility = View.VISIBLE
    }

    private fun invisibleButtons(btn1: Button, btn2: Button) {
        btn1.visibility = View.GONE
        btn2.visibility = View.GONE
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Checks if permission has already been granted
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission has already been granted, no need to do anything
                }
                else -> {
                    // Request user permission
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // In versions prior to Android 13, notification permissions are granted automatically
        }
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipInt = wifiManager.connectionInfo.ipAddress
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            ipInt and 0xff,
            (ipInt shr 8) and 0xff,
            (ipInt shr 16) and 0xff,
            (ipInt shr 24) and 0xff
        )
    }

    private fun isPrivateAddress(ip: String?): Boolean {
        if (!ip.isNullOrEmpty()) {
            val pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.\\d+\\.\\d+")
            val matcher = pattern.matcher(ip)
            if (matcher.find()) {
                val firstMember = matcher.group(1)?.toIntOrNull()
                val secondMember = matcher.group(2)?.toIntOrNull()
                if (firstMember != null && secondMember != null) {
                    return (firstMember == 10) || // 10.0.0.0 – 10.255.255.255
                            (firstMember == 172 && secondMember in 16..31) || // 172.16.0.0 – 172.31.255.255
                            (firstMember == 192 && secondMember == 168) // 192.168.0.0 – 192.168.255.255
                }
            }
        }
        return false
    }


    override fun onPortEntered(port: Int) {
        portValue = port
        Toast.makeText(this, "Received port: $portValue", Toast.LENGTH_SHORT).show()
        rvcwURL = "http://$localIp:$portValue/?authKey=${AuthKeyProvider.secretKey}"
    }

    override fun onAuthKeyEntered(secretKey: String) {
        AuthKeyProvider.secretKey = secretKey
        rvcwURL = "http://$localIp:$portValue/?authKey=${AuthKeyProvider.secretKey}"

    }

    private fun loadAuthKey(): String {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("authKey", "3x@mplE_S3crEt_K3y!") ?: "3x@mplE_S3crEt_K3y!"
    }

    private fun loadPort(): Int {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("port", 9090)
    }
}