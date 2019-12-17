package com.example.network.statistic

fun getApps(string: String): ArrayList<Pair<Int, String>> {
    val apps = string.split(",").mapNotNull {
        if (it.isNotEmpty()) {
            val data = it.split("-")
            Pair(data[0].toInt(), data[1])
        } else {
            null
        }
    }
    return ArrayList(apps)
}