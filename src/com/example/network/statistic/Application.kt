package com.example.network.statistic

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.request.receiveText
import java.lang.Exception
import java.lang.StringBuilder

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Apache) {
    }

    routing {
        put("user/add") {
            try {
                val parameters = call.request.queryParameters
                val id = parameters["name"]
                val user = "user$id"
                val isExisted = !Db.addUser(user)
                val existText = if (isExisted) {
                    "$user is already exist"
                } else {
                    "$user is added"
                }
                call.respondText(
                    "HELLO, $user!\n$existText",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(
                    "error",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.ExpectationFailed
                )
            }
        }

        get("user/get") {
            try {
                val sb = StringBuilder()
                Db.getUsers().forEach {
                    sb.appendln(it)
                }
                call.respondText(
                    sb.toString(),
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(
                    "error",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.ExpectationFailed
                )
            }
        }

        put("user/apps") {
            try {
                val text = call.receiveText().split("|")
                val user = text[0]
                val apps = text[1]
                Db.addApps(user, getApps(apps))
                call.respondText(
                    "Success",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(
                    e.getError(),
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.ExpectationFailed
                )
            }
        }

        get("user/apps") {
            try {
                val parameters = call.request.queryParameters
                parameters["name"]?.let { userId ->
                    val users = Db.getAppsForUser(userId)
                    call.respondText(
                        users.toString(),
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(
                    e.getError(),
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.ExpectationFailed
                )
            }
        }


        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

fun Exception.getError(): String = if (message == USER_DOESNT_EXIST) USER_DOESNT_EXIST else "error"

