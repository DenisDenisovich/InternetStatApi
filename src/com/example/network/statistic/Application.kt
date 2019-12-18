package com.example.network.statistic

import com.example.network.statistic.db.Db
import com.example.network.statistic.db.DbHelper
import com.example.network.statistic.models.UserApplicationResponse
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import com.fasterxml.jackson.databind.*
import com.google.gson.Gson
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.request.receiveText
import java.lang.Exception

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val db = DbHelper()
    val gson = Gson()
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
                val isAdded = db.addUser(user)
                val existText = if (isAdded) {
                    "$user is added"
                } else {
                    "$user is already exist"
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
                call.respondText(
                    gson.toJson(db.getUsers()),
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
                val text = call.receiveText()
                val userApps = gson.fromJson(text, UserApplicationResponse::class.java)
                db.addUserApps(userApps)
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
                call.request.queryParameters["name"]?.let { userId ->
                    val users = db.getUserApps(userId)
                    call.respondText(
                        gson.toJson(users),
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

