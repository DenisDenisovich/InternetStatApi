package com.example.network.statistic.domian.user

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.User
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GetUsersUseCase : UseCase<ArrayList<User>>() {

    override fun execute(): ArrayList<User> {
        val users = arrayListOf<User>()
        transaction {
            Db.Users.selectAll().forEach {
                users.add(User(it[Db.Users.userId], it[Db.Users.info] ?: "null"))
            }
        }
        return users
    }
}