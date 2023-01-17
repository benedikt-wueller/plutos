package dev.benedikt.plutos

import dev.benedikt.plutos.api.WebServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI

fun main() {
    DatabaseService.setup()

    WebServer.start()

    // Stop server if no client is connected.
    GlobalScope.launch {
        delay(10000)
        while (true) {
            if (System.currentTimeMillis() - WebServer.lastPing > 10000) break
            delay(1000)
        }
        WebServer.stop()
    }

    Desktop.getDesktop().browse(URI.create("http://localhost:8143/"))
//    Desktop.getDesktop().browse(URI.create("http://localhost:5173/"))

    WebServer.wait()
}
