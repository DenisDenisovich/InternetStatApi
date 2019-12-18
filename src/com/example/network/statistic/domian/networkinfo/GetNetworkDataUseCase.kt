package com.example.network.statistic.domian.networkinfo

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.NetworkData
import com.example.network.statistic.models.NetworkPeriod
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetNetworkDataUseCase(
    private val user: String,
    private val period: NetworkPeriod,
    private val startTime: Long,
    private val endTime: Long
) : UseCase<ArrayList<NetworkData>>() {

    override fun execute(): ArrayList<NetworkData> {
        val result = arrayListOf<NetworkData>()
        val condition = Db.NetworkData.userId eq user and
                (Db.NetworkData.period eq period.name) and
                (Db.NetworkData.timestamp greaterEq startTime) and
                (Db.NetworkData.timestamp lessEq endTime)
        transaction {
            Db.NetworkData
                .select(condition)
                .orderBy(Db.NetworkData.timestamp to SortOrder.ASC)
                .forEach {
                    result.add(
                        NetworkData(
                            user,
                            it[Db.NetworkData.timestamp],
                            period,
                            it[Db.NetworkData.data]
                        )
                    )
                }
        }
        return result
    }
}