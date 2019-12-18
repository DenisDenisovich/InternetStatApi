package com.example.network.statistic.models

data class NetworkData(
    val user: String,
    val timestamp: Long,
    val period: NetworkPeriod,
    val data: String
)

enum class NetworkPeriod{
    MINUTES,
    HOUR,
    DAY
}