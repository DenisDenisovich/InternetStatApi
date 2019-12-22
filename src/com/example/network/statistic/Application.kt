package com.example.network.statistic

import com.example.network.statistic.db.DbHelper
import com.example.network.statistic.models.NetworkData
import com.example.network.statistic.models.NetworkPeriod
import com.example.network.statistic.models.UserApplicationResponse
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import com.fasterxml.jackson.databind.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        put("user") {
            try {
                val parameters = call.request.queryParameters
                val user = parameters["name"]
                if (user != null) {
                    val isAdded = db.addUser(user)
                    val existText = if (isAdded) {
                        "$user is added"
                    } else {
                        "$user is already exist"
                    }
                    call.respond(HttpStatusCode.OK, AddUserResponse("HELLO, $user! $existText"))
                } else {
                    call.respond(HttpStatusCode.OK, Error("user is not specified"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, Error(e.getError()))
            }
        }

        get("user") {
            try {
                call.respond(HttpStatusCode.OK, GetUsersResponse(db.getUsers()))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, Error(e.getError()))
            }
        }

        put("apps") {
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

        get("apps") {
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

        put("networkdata") {
            try {
                val text = call.receiveText()
                val networkData = gson.fromJson<ArrayList<NetworkData>>(text)
                if (networkData != null) {
                    val name = networkData?.getOrNull(0)?.user
                    name?.let {
                        db.addNetworkData(it, networkData)
                    }
                    call.respondText(
                        "Success",
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                } else {
                    call.respondText(
                        "Error",
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

        get("networkdata") {
            try {
                val result = arrayListOf<NetworkData>()
                val parameters = call.request.queryParameters
                val user = parameters["name"]
                val period = parameters["period"]?.toUpperCase()
                val startTime = parameters["startTime"]?.toLongOrNull()
                val endTime = parameters["endTime"]?.toLongOrNull()
                var errorText: String? = null
                if (user == null) {
                    errorText = "name not specified"
                } else if (period == null || !NetworkPeriod.isExist(period)) {
                    errorText = "period not specified or incorrect"
                } else if (startTime == null) {
                    errorText = "startTime not specified or incorrect"
                } else if (endTime == null) {
                    errorText = "endTime not specified or incorrect"
                } else {
                    result.addAll(db.getNetworkData(user, NetworkPeriod.valueOf(period), startTime, endTime))
                }
                if (errorText == null) {
                    call.respondText(
                        gson.toJson(result),
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                } else {
                    call.respondText(
                        errorText,
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.ExpectationFailed
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

        get("networkdata/last") {
            try {
                val parameters = call.request.queryParameters
                val user = parameters["name"]
                val period = parameters["period"]?.toUpperCase()?.toUpperCase()
                var errorText: String? = null
                if (user == null) {
                    errorText = "name not specified"
                } else if (period == null || !NetworkPeriod.isExist(period)) {
                    errorText = "period not specified or incorrect"
                } else {
                    val lastTimestamp = user.let {
                        period.let {
                            db.getLastNetworkTimestamp(user, NetworkPeriod.valueOf(period))
                        }
                    }
                    call.respondText(
                        lastTimestamp.toString(),
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                }
                if (errorText != null) {
                    call.respondText(
                        errorText ?: "error",
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                }
            } catch (e: Exception) {
                call.respondText(
                    e.getError(),
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
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

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

data class AddUserResponse(
    val message: String
)

data class GetUsersResponse(val users: ArrayList<String>)

data class Error(val message: String)