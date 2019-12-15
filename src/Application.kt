package com.example.network.statistic

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.request
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                val userID = "user${(Math.random() * 1000).toInt()}"
                call.respondText("Hello World! $userID", ContentType.Text.Plain)
                Db.addUser(userID)
            }
            get("/demo") {

                val sb = StringBuilder()
                Db.getUsers().forEach {
                    sb.appendln(it)
                }
                call.respondText("HELLO WORLD!\nsb")
            }
        }
    }
    server.start(wait = true)
}

private fun addTestUSer() {
    val user = "USer1"
    Db.addUser(user)
}

private fun getUser() {

}