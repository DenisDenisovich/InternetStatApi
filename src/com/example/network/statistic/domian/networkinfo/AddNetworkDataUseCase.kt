package com.example.network.statistic.domian.networkinfo

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.NetworkData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AddNetworkDataUseCase(private val networkData: NetworkData) : UseCase<Unit>() {

    override fun execute() {
        val condition = Db.NetworkData.userId eq networkData.user and
                (Db.NetworkData.timestamp eq networkData.timestamp) and
                (Db.NetworkData.period eq networkData.period.name)

        val existedUserData = transaction {
            Db.NetworkData.select(condition).singleOrNull()
        }?.getOrNull(Db.UserApplications.userId)
        // add data to db
        if (existedUserData == null) {
            transaction {
                Db.NetworkData.insert {
                    it[userId] = networkData.user
                    it[timestamp] = networkData.timestamp
                    it[period] = networkData.period.name
                    it[data] = networkData.data
                }
            }
        } else {
            transaction {
                Db.NetworkData.update({condition}) {
                    it[userId] = networkData.user
                    it[timestamp] = networkData.timestamp
                    it[period] = networkData.period.name
                    it[data] = networkData.data
                }
            }
        }
    }
}