package com.example.network.statistic

import com.example.network.statistic.models.Application
import com.example.network.statistic.models.UserApplicationResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.lang.StringBuilder
import javax.jws.soap.SOAPBinding

object Db {

    private val gson = Gson()
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

    fun addApps(userApps: UserApplicationResponse) {
        checkUserIsExist(userApps.user)
        val appsString = gson.toJson(userApps.apps)
        // check update or insert operation
        val existedUserID = transaction {
            UserApplications
                .select { UserApplications.userId eq userApps.user }
                .singleOrNull()
        }?.getOrNull(UserApplications.userId)
        // add data to db
        if (existedUserID == null) {
            transaction {
                UserApplications.insert {
                    it[userId] = userApps.user
                    it[apps] = appsString
                }
            }
        } else {
            transaction {
                UserApplications.update({ UserApplications.userId eq userApps.user }) { it[apps] = appsString }
            }
        }
    }

    fun getAppsForUser(user: String): ArrayList<Application> {
        checkUserIsExist(user)
        val apps = transaction {
            UserApplications
                .select { UserApplications.userId eq user }
                .singleOrNull()
                ?.getOrNull(UserApplications.apps)
        }
        return if (apps != null) {
            gson.fromJson<ArrayList<Application>>(apps)
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

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
}