package com.example.network.statistic.main.kotlin

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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