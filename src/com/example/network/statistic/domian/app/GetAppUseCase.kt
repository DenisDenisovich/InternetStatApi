package com.example.network.statistic.domian.app

import com.example.network.statistic.db.Db
import com.example.network.statistic.db.DbHelper
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.Application
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetAppUseCase(private val user: String): UseCase<ArrayList<Application>>() {

    override fun execute(): ArrayList<Application> {
        val apps = transaction {
            Db.UserApplications
                .select { Db.UserApplications.userId eq user }
                .singleOrNull()
                ?.getOrNull(Db.UserApplications.apps)
        }
        return if (apps != null) {
            val result = gson.fromJson<ArrayList<Application>>(apps)
            result.forEach {
                it.category = DbHelper.getCategory(it.name) ?: "null"
            }
            result
        } else {
            ArrayList()
        }
    }
}