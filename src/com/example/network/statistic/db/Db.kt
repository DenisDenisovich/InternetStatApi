package com.example.network.statistic.db

import com.example.network.statistic.db.Db.UserApplications.primaryKey
import org.jetbrains.exposed.sql.*

object Db {

    object Users : Table() {
        val userId = varchar("user_id", 50).primaryKey()
        val info = varchar("info", 150).nullable()
    }

    object UserApplications : Table() {
        val userId = varchar("user_id", 50).primaryKey()
        val apps = text("apps")
    }

    object NetworkData : Table() {
        val id = integer("id").autoIncrement().primaryKey() // Column<String>
        val userId = varchar("user_id", 50).index()
        val timestamp = long("timestamp").index()
        val period =varchar("period", 10)
        val data = text("data")
    }

    object AppCategory: Table() {
        val app = varchar("app", 100).primaryKey()
        val category = varchar("category", 50)
    }
}