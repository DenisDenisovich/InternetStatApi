package com.example.network.statistic.models

data class UserApplicationResponse(val user: String, val apps: List<Application>)

data class Application(val id: String, val name: String, var category: String)