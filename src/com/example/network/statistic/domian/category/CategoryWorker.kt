package com.example.network.statistic.domian.category

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.ClientRequestException
import io.ktor.client.response.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.Exception

class CategoryWorker {

    val client = HttpClient(Apache)
    suspend fun checkApp(app: String): String? {
        delay(getDelay() * 1000L)
        val response =
            withContext(Dispatchers.IO) {
                try {
                    client.call(GOOGLE_PLAY_URL + app).response.readText()
                } catch (e: Exception) {
                    var result: String? = null
                    if (e is ClientRequestException) {
                        if (e.response.status.value == 404) {
                            result = UNDEFINED
                        }
                    } else {
                        // TODO log event to metric there
                        result = null
                    }
                    result
                }
            }
        return if (response == UNDEFINED) {
            UNDEFINED
        } else {
            response?.let {
                val categorySubstring = REGEX.toRegex().find(response)
                val categoryLink = categorySubstring?.groups?.lastOrNull()?.value
                categoryLink?.split("/")?.lastOrNull()
            }
        }
    }

    private fun getDelay() = (Math.random() * MAX_REQUEST_ADDITION).toInt() + MIN_REQUEST_DELAY

    companion object {
        private const val REGEX = "(itemprop=\"genre\" href=)\"(\\S*)\""
        private const val UNDEFINED = "undefined"
        private const val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="
        private const val MIN_REQUEST_DELAY = 5
        private const val MAX_REQUEST_ADDITION = 5
    }
}