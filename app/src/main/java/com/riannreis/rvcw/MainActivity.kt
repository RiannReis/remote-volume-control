package com.riannreis.rvcw

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var openDialogPortBtn: Button
    private lateinit var openDialogAuthBtn: Button
    private lateinit var startOrEndRemoteControlBtn: Button
    private lateinit var closeBtn: Button
    private lateinit var txtCloseDesc: TextView
    private lateinit var txtDesc: TextView
    private lateinit var webLink: TextView
    private var start: Boolean = false
    private var serverIpIsPrivate: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        openDialogPortBtn = findViewById(R.id.btn_dialog_choose_port)
        openDialogAuthBtn = findViewById(R.id.btn_dialog_basic_auth)
        startOrEndRemoteControlBtn = findViewById(R.id.btn_start_or_end_remote_control)
        webLink = findViewById(R.id.web_link)
        closeBtn = findViewById(R.id.btn_close)
        txtCloseDesc = findViewById(R.id.txt_close_desc)
        txtDesc = findViewById(R.id.txt_desc)

        // Main Button (Enable / Disable)

        startOrEndRemoteControlBtn.setOnClickListener {
            start = !start
            updateActivity()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saves the 'start' state (whether it is 'running' or not)
        outState.putBoolean("isRunning", start)

        // Saves the link text (if visible)
        outState.putString("webLinkText", webLink.text.toString())

        // Save link visibility
        outState.putBoolean("isWebLinkVisible", webLink.visibility == View.VISIBLE)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restores the 'start' state
        start = savedInstanceState.getBoolean("isRunning", false)

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
        if (start) {
            if (serverIpIsPrivate) {
                startOrEndRemoteControlBtn.setText(R.string.running)
                txtDesc.setText(R.string.web_remote_volume_control_enabled)
                webLink.visibility = View.VISIBLE
                webLink.text = "http://10.0.0.231:9000"
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

}