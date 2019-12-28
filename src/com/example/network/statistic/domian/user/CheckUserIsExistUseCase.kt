package com.example.network.statistic.domian.user

import com.example.network.statistic.USER_DOESNT_EXIST
import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception

class CheckUserIsExistUseCase(private val user: String) : UseCase<Unit>() {

    override fun execute() {
        transaction {
            Db.Users.select { Db.Users.userId eq user }
                .singleOrNull()
        }?.getOrNull(Db.Users.userId) ?: throw Exception(
            USER_DOESNT_EXIST
        )
    }
}