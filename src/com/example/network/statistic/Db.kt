package com.example.network.statistic

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.lang.StringBuilder
import javax.jws.soap.SOAPBinding

object Db {

    init {
        Database.connect(
            "jdbc:postgresql://shefer.space/vdenisov_diplom", driver = "org.postgresql.Driver",
            user = "vdenisov_diplom", password = "Js!t7B8AG#CM5v9&"
        )
    }

    object Users : Table() {
        val userId = varchar("user_id", 50).primaryKey()
    }

    object NetworkData : Table() {
        val id = integer("id").autoIncrement().primaryKey() // Column<String>
        val userId = varchar("user_id", 50)
        val data = varchar("data", 300)
    }

    object UserApplications : Table() {
        val userId = varchar("user_id", 50).primaryKey()
        val apps = text("apps")
    }

    init {
        transaction {
            SchemaUtils.create(NetworkData, Users, UserApplications)
        }
    }

    fun addApps(user: String, apps: ArrayList<Pair<Int, String>>) {
        checkUserIsExist(user)
        // parse data
        val sb = StringBuilder()
        apps.forEach { (id, packageName) ->
            sb.append("$id-$packageName,")
        }
        val appsString = sb.toString()
        // check update or insert operation
        val existedUserID = transaction {
            UserApplications
                .select { UserApplications.userId eq user }
                .singleOrNull()
        }?.getOrNull(UserApplications.userId)
        // add data to db
        if (existedUserID == null) {
            transaction {
                UserApplications.insert {
                    it[UserApplications.userId] = user
                    it[UserApplications.apps] = appsString
                }
            }
        } else {
            transaction {
                UserApplications.update({ UserApplications.userId eq user }) { it[UserApplications.apps] = appsString }
            }
        }
    }

    fun getAppsForUser(user: String): ArrayList<Pair<Int, String>> {
        checkUserIsExist(user)
        val apps = transaction {
            UserApplications
                .select { UserApplications.userId eq user }
                .singleOrNull()
                ?.getOrNull(UserApplications.apps)
        }
        return if (apps != null) {
            getApps(apps)
        } else {
            ArrayList()
        }
    }

    fun addNetworkData(user: String, data: String) {
        NetworkData.insert {
            it[userId] = user
            it[NetworkData.data] = data
        }
    }

    /**
     * @return false, if already exist
     * */
    fun addUser(user: String): Boolean {
        val existedUserID = transaction {
            Users.select { Users.userId eq user }.singleOrNull()
        }?.getOrNull(
            Users.userId
        )
        if (existedUserID == null) {
            transaction {
                Users.insert {
                    it[userId] = user
                }
            }
        }
        return existedUserID == null
    }

    fun getUsers(): ArrayList<String> {
        val users = arrayListOf<String>()
        transaction {
            Users.selectAll().forEach {
                users.add(it[Users.userId])
            }
        }
        return users
    }

    private fun checkUserIsExist(user: String) {
        // check if user exist
        transaction {
            Users.select { Users.userId eq user }
                .singleOrNull()
        }?.getOrNull(Users.userId) ?: throw Exception(USER_DOESNT_EXIST)
    }
}