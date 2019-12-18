package com.example.network.statistic.domian.networdata

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.NetworkData
import com.example.network.statistic.models.NetworkPeriod
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class GetNetworkDataUseCase(
    private val user: String,
    private val period: NetworkPeriod,
    private val startTime: Long,
    private val endTime: Long
) : UseCase<ArrayList<NetworkData>>() {
    //val data = ArrayList<NetworkData>()
    override fun execute(): ArrayList<NetworkData> {
        //getAll()
        val result = arrayListOf<NetworkData>()
        val condition = Op.build {
            (Db.NetworkData.userId eq user) and
                    (Db.NetworkData.period eq period.name) and
                    (Db.NetworkData.timestamp greaterEq startTime) and
                    (Db.NetworkData.timestamp lessEq  endTime)
        }
        transaction {
            Db.NetworkData
                .select(condition)
                .orderBy(Db.NetworkData.timestamp to SortOrder.ASC)
                .forEach { row ->
                    result.add(
                        NetworkData(
                            user,
                            row[Db.NetworkData.timestamp],
                            period,
                            row[Db.NetworkData.data]
                        )
                    )
                }
        }
        return result
    }

/*
    private fun getAll() {
        transaction {
            Db.NetworkData.selectAll().forEach {
                data.add(
                    NetworkData(
                        it[Db.NetworkData.userId],
                        it[Db.NetworkData.timestamp],
                        NetworkPeriod.valueOf(it[Db.NetworkData.period]),
                        it[Db.NetworkData.data]
                    )
                )
            }
        }
    }
*/
}