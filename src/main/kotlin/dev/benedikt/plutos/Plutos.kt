package dev.benedikt.plutos

import dev.benedikt.plutos.api.WebServer

fun main() {
    DatabaseService.setup()
    WebServer.run()
}
