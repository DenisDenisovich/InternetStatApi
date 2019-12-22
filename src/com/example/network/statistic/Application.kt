package com.example.network.statistic

import com.example.network.statistic.db.DbHelper
import com.example.network.statistic.models.NetworkData
import com.example.network.statistic.models.NetworkPeriod
import com.example.network.statistic.models.UserApplicationResponse
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
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
                    call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse("user is not specified"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
            }
        }

        get("user") {
            try {
                call.respond(HttpStatusCode.OK, GetUsersResponse(db.getUsers()))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
            }
        }

        put("apps") {
            try {
                val text = call.receiveText()
                val userApps = gson.fromJson(text, UserApplicationResponse::class.java)
                db.addUserApps(userApps)
                call.respond(HttpStatusCode.OK, SuccessResponse())
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
            }
        }

        get("apps") {
            try {
                call.request.queryParameters["name"]?.let { userId ->
                    call.respond(HttpStatusCode.OK, GetUserAppsResponse(db.getUserApps(userId)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
            }
        }

        put("networkdata") {
            try {
                val text = call.receiveText()
                val networkData = gson.fromJson<ArrayList<NetworkData>>(text)
                if (networkData != null) {
                    val name = networkData.getOrNull(0)?.user
                    name?.let {
                        db.addNetworkData(it, networkData)
                    }
                    call.respond(HttpStatusCode.OK, SuccessResponse())
                } else {
                    call.respond(HttpStatusCode.ExpectationFailed, SuccessResponse("error"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
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
                    call.respond(HttpStatusCode.OK, GetUserNetworkResponse(result))
                } else {
                    call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(errorText))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
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
                    val lastTimestamp: Long = user.let {
                        period.let {
                            db.getLastNetworkTimestamp(user, NetworkPeriod.valueOf(period))
                        }
                    }
                    call.respond(HttpStatusCode.OK, GetLastNetworkResponse(lastTimestamp))
                }
                if (errorText != null) {
                    call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(errorText ?: "error"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ExpectationFailed, ErrorResponse(e.getError()))
            }
        }
    }
}

fun Exception.getError(): String = if (message == USER_DOESNT_EXIST) USER_DOESNT_EXIST else "error"

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

data class AddUserResponse(
    val message: String
)

data class GetUsersResponse(val users: ArrayList<String>)

data class ErrorResponse(val message: String)

data class SuccessResponse(val message: String = "Success")

data class GetUserAppsResponse(val apps: ArrayList<com.example.network.statistic.models.Application>)

data class GetUserNetworkResponse(val networkData: ArrayList<NetworkData>)

data class GetLastNetworkResponse(val lasTime: Long)