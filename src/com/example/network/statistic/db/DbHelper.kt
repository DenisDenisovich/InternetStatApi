package com.example.network.statistic.db

import com.example.network.statistic.domian.CheckUserIsExistUseCase
import com.example.network.statistic.domian.app.AddAppUseCase
import com.example.network.statistic.domian.app.GetAppUseCase
import com.example.network.statistic.domian.genstat.AddMalwareStatUseCase
import com.example.network.statistic.domian.malwaredetect.AddMalwareSearchResultUseCase
import com.example.network.statistic.domian.networdata.AddNetworkDataUseCase
import com.example.network.statistic.domian.networdata.GetLastNetworkDataUseCase
import com.example.network.statistic.domian.networdata.GetNetworkDataUseCase
import com.example.network.statistic.domian.user.AddUserUseCase
import com.example.network.statistic.domian.user.GetUsersUseCase
import com.example.network.statistic.models.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object DbHelper {

    init {
        Database.connect(
            "jdbc:postgresql://shefer.space/vdenisov_diplom", driver = "org.postgresql.Driver",
            user = "vdenisov_diplom", password = "Js!t7B8AG#CM5v9&"
        )
        transaction {
            SchemaUtils.create(
                Db.NetworkData,
                Db.Users,
                Db.UserApplications,
                Db.AppMalware2
            )
/*
            SchemaUtils.createMissingTablesAndColumns(
                Db.NetworkData,
                Db.Users,
                Db.UserApplications,
                Db.AppCategory,
                Db.AppMalware2
            )
*/
        }
    }

    fun addUser(user: User): Boolean = AddUserUseCase(user).execute()

    fun getUsers(): ArrayList<User> = GetUsersUseCase().execute()

    fun addUserApps(userApps: UserApplicationResponse) {
        CheckUserIsExistUseCase(userApps.user).execute()
        AddAppUseCase(userApps).execute()
    }

    fun getUserApps(user: String, withCategories: Boolean = true): ArrayList<Application> {
        CheckUserIsExistUseCase(user).execute()
        return GetAppUseCase(user, withCategories).execute()
    }

    fun addNetworkData(user: String, networkData: ArrayList<NetworkData>) {
        CheckUserIsExistUseCase(user).execute()
        networkData.forEach {
            AddNetworkDataUseCase(it).execute()
        }
    }

    fun getNetworkData(
        user: String,
        period: NetworkPeriod,
        startTime: Long,
        endTime: Long
    ): ArrayList<NetworkData> {
        CheckUserIsExistUseCase(user).execute()
        return GetNetworkDataUseCase(user, period, startTime, endTime).execute()
    }

    fun getLastNetworkTimestamp(user: String, period: NetworkPeriod): Long {
        CheckUserIsExistUseCase(user).execute()
        return GetLastNetworkDataUseCase(user, period).execute()
    }

    fun categoryIsExist(app: String): Boolean {
        val existedApp = transaction {
            Db.AppCategory
                .select { Db.AppCategory.app eq app }
                .singleOrNull()
                ?.getOrNull(Db.AppCategory.app)
        }
        return existedApp != null
    }

    @Synchronized
    fun getCategory(app: String): String? =
        transaction {
            Db.AppCategory
                .select { Db.AppCategory.app eq app }
                .singleOrNull()
                ?.getOrNull(Db.AppCategory.category)
        }

    fun getMalware(user: String) {
        //CheckUserIsExistUseCase(user).execute()
        AddMalwareSearchResultUseCase(user, arrayListOf()).execute()
    }
}