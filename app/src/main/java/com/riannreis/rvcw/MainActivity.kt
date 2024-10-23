package com.riannreis.rvcw

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.riannreis.rvcw.server.WebServer
import com.riannreis.rvcw.server.WebServerService

class MainActivity : AppCompatActivity(), InputPortDialogFragment.PortDialogListener {

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

    private lateinit var webServer: WebServer

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

        checkAndRequestNotificationPermission()

        openDialogPortBtn = findViewById(R.id.btn_dialog_choose_port)
        openDialogAuthBtn = findViewById(R.id.btn_dialog_basic_auth)
        startOrEndRemoteControlBtn = findViewById(R.id.btn_start_or_end_remote_control)
        webLink = findViewById(R.id.web_link)
        closeBtn = findViewById(R.id.btn_close)
        txtCloseDesc = findViewById(R.id.txt_close_desc)
        txtDesc = findViewById(R.id.txt_desc)

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

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(serverStateReceiver, IntentFilter("SERVER_STATE_UPDATE"),
                    RECEIVER_NOT_EXPORTED
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(serverStateReceiver)
    }


    private fun startRemoteControlService() {
        val intent = Intent(this, WebServerService::class.java)
        intent.putExtra("PORT_VALUE", portValue)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saves the 'start' state (whether it is 'running' or not)

        // Saves the link text (if visible)
        outState.putString("webLinkText", webLink.text.toString())

        // Save link visibility
        outState.putBoolean("isWebLinkVisible", webLink.visibility == View.VISIBLE)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restores the 'start' state

        // Restores the link if visible
        val webLinkText = savedInstanceState.getString("webLinkText")
        val isWebLinkVisible = savedInstanceState.getBoolean("isWebLinkVisible", false)

        if (isWebLinkVisible) {
            webLink.visibility = View.VISIBLE
            webLink.text = webLinkText
        } else {
            webLink.visibility = View.GONE
        }

        // Updates interface
        updateActivity()
    }


    private fun updateActivity() {
        if (isRunning) {
            if (serverIpIsPrivate) {
                startOrEndRemoteControlBtn.setText(R.string.running)
                txtDesc.setText(R.string.web_remote_volume_control_enabled)
                webLink.visibility = View.VISIBLE
                webLink.text = "http://localhost:$portValue"
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

    override fun onPortEntered(port: Int) {
        portValue = port
        Toast.makeText(this, "Received port: $portValue", Toast.LENGTH_SHORT).show()
    }
}