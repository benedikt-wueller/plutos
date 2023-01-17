package dev.benedikt.plutos

import dev.benedikt.plutos.api.WebServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    WebServer.wait()
}
