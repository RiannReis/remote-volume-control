package com.riannreis.rvcw.server

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.riannreis.rvcw.dialogs.InputPortDialogFragment
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class WebServer(private val context: Context, private val audioManager: AudioManager) :
    InputPortDialogFragment.PortDialogListener {

    private var portValue = 9090

    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    fun startServer(port: Int) {
        if (server == null || port != portValue) {

            server?.stop(1000, 10000)

            portValue = port

            server = embeddedServer(Netty, port = portValue) {
                routing {
                    get("/") { call.respondText("Hello, world!") }

                    get("/up") { setVolumeUp() }

                    get("/down") { setVolumeDown() }

                    get("/volume/{volume}") {
                        val volume = call.parameters["volume"]?.toIntOrNull()
                        if (volume != null) {
                            setVolume(volume)
                        }
                    }
                }
            }

            try {
                server?.start(wait = false)

                Log.d("WebServer", "Server started on port $portValue")
            } catch (e: Exception) {
                Log.e("WebServer", "Error starting server on port $portValue", e)
            }
        } else {
            Log.d("WebServer", "Server is already running on port $portValue")
        }

    }

    fun stopServer() {
        server?.stop(1000, 10000)
        Log.d("WebServer", "Server stopped")
    }

    private fun setVolumeUp() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
        Log.d("WebServer", "Volume up")
    }

    private fun setVolumeDown() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
        Log.d("WebServer", "Volume down")
    }

    private fun setVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val adjustedVolume = when {
            volume < 0 -> 0
            volume > maxVolume -> maxVolume
            else -> volume
        }
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            adjustedVolume,
            AudioManager.FLAG_PLAY_SOUND
        )
    }


    override fun onPortEntered(port: Int) {
        portValue = port
    }
}