package com.riannreis.rvcw.server

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.riannreis.rvcw.AuthKeyProvider
import com.riannreis.rvcw.dialogs.InputPortDialogFragment
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.onChange
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

class WebServer(
    private val context: Context,
    private val audioManager: AudioManager
) :
    InputPortDialogFragment.PortDialogListener {

    private var portValue = 9090

    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    fun startServer(port: Int) {
        if (server == null || port != portValue) {

            server?.stop(1000, 10000)

            portValue = port

            server = embeddedServer(Netty, port = portValue) {

                install(StatusPages) {
                    exception<Throwable> { call, cause ->
                        call.respondText(
                            text = "500: $cause",
                            status = HttpStatusCode.InternalServerError
                        )
                    }
                }

                routing {
                    get("/") {

                        val authKey = call.parameters["authKey"]

                        if (authKey == AuthKeyProvider.secretKey) {
                            Log.d("WebServer", "Authentication successful")


                            call.respondHtml(HttpStatusCode.OK) {
                                val currentVolume =
                                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val maxVolume =
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)



                                head {
                                    title { +"Volume Control" }

                                    style {
                                        unsafe {
                                            raw(
                                                """
                                                body {
                                                    display: flex;
                                                    flex-direction: column;
                                                    justify-content: center;
                                                    align-items: center;
                                                    height: 100vh;
                                                    font-family: Arial, sans-serif;
                                                    background-color: #f2f2f2;
                                                    margin: 0;
                                                }
                                                .container {
                                                    display: flex;
                                                    justify-content: space-between;
                                                    width: 60%;
                                                    max-width: 600px;
                                                    background-color: #fff;
                                                    padding: 20px;
                                                    border-radius: 8px;
                                                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                                                }
                                                .buttons {
                                                    display: flex;
                                                    flex-direction: column;
                                                    align-items: center;
                                                    gap: 20px;
                                                }
                                                .buttons button {
                                                    width: 120px;
                                                    padding: 10px;
                                                    font-size: 16px;
                                                    cursor: pointer;
                                                    background-color: #007bff;
                                                    color: #fff;
                                                    border: none;
                                                    border-radius: 4px;
                                                }
                                                .buttons button:hover {
                                                    background-color: #0056b3;
                                                }
                                                .slider-container {
                                                    display: flex;
                                                    flex-direction: column;
                                                    align-items: center;
                                                    gap: 10px;
                                                }
                                                .slider-container input[type="range"] {
                                                    width: 200px;
                                                }
                                                .slider-container p {
                                                    font-size: 14px;
                                                    color: #666;
                                                }
                                            """.trimIndent()
                                            )
                                        }
                                    }

                                    script {
                                        unsafe {
                                            raw(
                                                """
                                                function asyncVolume(action) {
                                                    var xhr = new XMLHttpRequest();
                                                    xhr.open("GET", "/" + action, true);
                                                    xhr.send();
                                                    }
                                                    
                                                function setVolume(volume) {
                                                    var xhr = new XMLHttpRequest();
                                                    xhr.open("GET", "/volume/" + volume, true);
                                                    xhr.send();
                                                    }
                                                """.trimIndent()
                                            )
                                        }
                                    }
                                }
                                body {
                                    h1 { +"Volume Control" }

                                    div("container") {
                                        div("buttons") {
                                            button {
                                                id = "up"
                                                onClick = "asyncVolume('up')"
                                                +"Volume Up"
                                            }

                                            button {
                                                id = "down"
                                                onClick = "asyncVolume('down')"
                                                +"Volume Down"
                                            }
                                        }

                                        div("slider-container") {
                                            input(type = InputType.range, name = "volume") {
                                                min = "0"
                                                max = maxVolume.toString()
                                                value = currentVolume.toString()
                                                onChange = "setVolume(this.value)"
                                            }
                                            p { +"Drag to adjust volume" }
                                        }
                                    }
                                }

                            }
                        } else {
                            Log.d("WebServer", "Authentication failed")
                            call.respondText(
                                text = "Authentication failed",
                                status = HttpStatusCode.Unauthorized
                            )
                        }
                    }

                    get("/up") {
                        setVolumeUp()
                        call.respondText("Volume up")
                    }

                    get("/down") {
                        setVolumeDown()
                        call.respondText("Volume down")
                    }

                    get("/volume/{volume}") {
                        val volume = call.parameters["volume"]?.toIntOrNull()
                        if (volume != null) {
                            setVolume(volume)
                            call.respondText("Volume adjusted to $volume")
                        } else {
                            call.respondText(
                                "Invalid volume value",
                                status = HttpStatusCode.BadRequest
                            )
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