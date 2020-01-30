package com.example.network.statistic.models

data class NetworkData(
    val user: String,
    val timestamp: Long,
    val period: NetworkPeriod,
    var data: String
)

enum class NetworkPeriod {
    MINUTES,
    HOUR,
    DAY;

    companion object {

        fun isExist(period: String?): Boolean {
            val values = NetworkPeriod.values().map { it.name }
            return values.contains(period)
        }
    }
}