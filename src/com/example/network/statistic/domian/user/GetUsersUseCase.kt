package com.example.network.statistic.domian.user

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GetUsersUseCase: UseCase<ArrayList<String>>() {

    override fun execute(): ArrayList<String> {
        val users = arrayListOf<String>()
        transaction {
            Db.Users.selectAll().forEach {
                users.add(it[Db.Users.userId])
            }
        }
        return users
    }
}