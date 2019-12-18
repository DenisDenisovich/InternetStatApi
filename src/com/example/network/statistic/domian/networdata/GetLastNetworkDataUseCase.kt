package com.example.network.statistic.domian.networdata

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.NetworkPeriod
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetLastNetworkDataUseCase(val name: String, val period: NetworkPeriod): UseCase<Long>() {

    override fun execute(): Long {
        val condition = Op.build {
            (Db.NetworkData.userId eq name) and
                    (Db.NetworkData.period eq period.name)
        }
        return transaction {
            Db.NetworkData.
                select(condition)
                .orderBy(Db.NetworkData.timestamp to SortOrder.ASC)
                .lastOrNull()
                ?.getOrNull(Db.NetworkData.timestamp) ?: 0L
        }
    }
}