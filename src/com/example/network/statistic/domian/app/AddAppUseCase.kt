package com.example.network.statistic.domian.app

import com.example.network.statistic.db.Db
import com.example.network.statistic.db.DbHelper
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.domian.category.CategoryUpdater
import com.example.network.statistic.models.UserApplicationResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AddAppUseCase(private val userApps: UserApplicationResponse): UseCase<Unit>() {

    override fun execute() {
        val appsString = gson.toJson(userApps.apps)
        // check update or insert operation
        val existedUserID = transaction {
            Db.UserApplications
                .select { Db.UserApplications.userId eq userApps.user }
                .singleOrNull()
        }?.getOrNull(Db.UserApplications.userId)
        // add data to db
        if (existedUserID == null) {
            transaction {
                Db.UserApplications.insert {
                    it[userId] = userApps.user
                    it[apps] = appsString
                }
            }
            CategoryUpdater.addAppsForCheck(userApps.apps.map { it.name })
        } else {
            updateNewUsersAppCategory()
            transaction {
                Db.UserApplications.update({ Db.UserApplications.userId eq userApps.user }) { it[apps] = appsString }
            }

        }
    }

    private fun updateNewUsersAppCategory() {
        val currentUserApps = GetAppUseCase(userApps.user).execute()
        val newUserApps = arrayListOf<String>()
        userApps.apps.forEach {
            if (!currentUserApps.contains(it)) {
                newUserApps.add(it.name)
            }
        }
        CategoryUpdater.addAppsForCheck(newUserApps)
    }
}
