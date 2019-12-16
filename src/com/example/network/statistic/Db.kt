package com.example.network.statistic

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.lang.StringBuilder

object Db {

    init {
        Database.connect("jdbc:postgresql://shefer.space/vdenisov_diplom", driver = "org.postgresql.Driver",
            user = "vdenisov_diplom", password = "Js!t7B8AG#CM5v9&")
    }

    object Users : Table() {
        val id = varchar("id", 50).autoIncrement().primaryKey() // Column<String>
        val userId = varchar("user_id", 50)
    }

    object NetworkData : Table() {
        val id = varchar("id", 50).autoIncrement().primaryKey() // Column<String>
        val userId = varchar("user_id", 50)
        val data = varchar("data", 300)
    }

    object UserApplications : Table() {
        val id = varchar("id", 50).autoIncrement().primaryKey() // Column<String>
        val userId = varchar("user_id", 50)
        val apps = text("apps")
    }

    fun addApps(user: String, apps: ArrayList<Pair<Int, String>>) {
        UserApplications.update({ UserApplications.userId eq user }) {
            val sb = StringBuilder()
            apps.forEach {
                sb.append("{${it.first}, ${it.second}}, ")
            }
            it[UserApplications.apps] = sb.toString()
        }
    }

    fun addNetworkData(user: String, data: String) {
        NetworkData.insert {
            it[userId] = user
            it[NetworkData.data] = data
        }
    }

    fun addUser(user: String) {
        val existedUserID = try {
            transaction { Users.select { Users.userId eq user }.single() }.getOrNull(
                Users.userId
            )
        } catch (e: Exception) {
            null
        }
        if (existedUserID == null) {
            Users.insert {
                it[userId] = user
            }
        }
    }

    fun getUsers(): ArrayList<String> {
        val users = arrayListOf<String>()
        Users.selectAll().forEach {
            users.add(it[Users.userId])
        }
        return users
    }
}